package worker

import java.nio.file.Path
import java.util.UUID

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.stream.Materializer
import akka.stream.alpakka.file.DirectoryChange
import akka.stream.alpakka.file.scaladsl.{ Directory, DirectoryChangesSource }
import akka.stream.scaladsl.Sink
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import worker.WorkManager.SubmitWork

import scala.concurrent.duration._

class FileIngestor(workManager: ActorRef[SubmitWork], ctx: ActorContext[FrontEnd.Command], fs: Path) {

  val listFiles = {
    Directory
      .ls(fs)
      .map { path =>
        println("Reading file -> " + path)
        (path, DirectoryChange.Creation)
      }
      .merge(DirectoryChangesSource(fs, pollInterval = 1.second, maxBufferSize = 1000))
  }

  def getFilename(path: Path): String = {
    val filename = path.getFileName.toFile.getName
    filename.substring(0, filename.lastIndexOf("."))
  }

  def toWork(filename: String): Work =
    Work(UUID.randomUUID().toString, filename)

  val readFromDirectory =
    listFiles
      .filterNot(_._2 == DirectoryChange.Deletion)
      .map(_._1)
      .map(getFilename)
      .map(toWork)

  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val mat              = Materializer.matFromSystem(ctx.system)

  readFromDirectory
    .via(ActorFlow.ask[Work, SubmitWork, WorkManager.Ack](1)(workManager)((work, replyTo) => SubmitWork(work, replyTo)))
    .runWith(Sink.ignore)
}
