package worker

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{ Cluster, SelfUp, Subscribe }
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.{ Config, ConfigFactory }
import org.slf4j.LoggerFactory

object Main {

  val log = LoggerFactory.getLogger(Main.getClass)

  // note that 2551 and 2552 are expected to be seed nodes though, even if
  // the back-end starts at 2000
  val backEndPortRange = 2000 to 2999

  val frontEndPortRange = 3000 to 3999

  def main(args: Array[String]): Unit =
    args.headOption match {

      case None =>
        startInK8s()

      case Some(portString) if portString.matches("""\d+""") =>
        val port = portString.toInt
        if (backEndPortRange.contains(port)) start(config(port, "back-end"))
        else if (frontEndPortRange.contains(port)) start(config(port, "front-end"))
        else start(config(port, "worker"), args.lift(1).map(_.toInt).getOrElse(1))

      case Some("local") =>
        startClusterInSameJvm()
    }

  def startInK8s() = {
    log.info("starting in Kubernetes")
    val system = ActorSystem(
      Behaviors.setup[SelfUp] { ctx =>
        val cluster = Cluster(ctx.system)
        cluster.subscriptions ! Subscribe(ctx.self, classOf[SelfUp])
        Behaviors.receiveMessage {
          case SelfUp(_) =>
            ctx.log.info("Node is up")
            if (cluster.selfMember.hasRole("back-end")) {
              WorkManagerSingleton.init(ctx.system)
            }
            if (cluster.selfMember.hasRole("front-end")) {
              val workManagerProxy = WorkManagerSingleton.init(ctx.system)
              ctx.spawn(FrontEnd(workManagerProxy), "front-end")
              ctx.spawn(WorkResultConsumer(), "consumer")
            }
            if (cluster.selfMember.hasRole("worker")) {
              (1 to 2).foreach(n => ctx.spawn(Worker(), s"worker-$n"))
            }
            Behaviors.same
        }
      },
      "ClusterSystem"
    )

    // Akka Management hosts the HTTP routes used by bootstrap
    AkkaManagement(system).start()

    // Starting the bootstrap process needs to be done explicitly
    ClusterBootstrap(system).start()
  }

  def startClusterInSameJvm(): Unit = {
    // two backend nodes
    start(config(2551, "back-end"))
    start(config(2552, "back-end"))
    // two front-end nodes
    start(config(3000, "front-end"))
    start(config(3001, "front-end"))
    // two worker nodes with two worker actors each
    start(config(5001, "worker"), 2)
    start(config(5002, "worker"), 2)
  }

  def start(config: Config, workers: Int = 2): Unit =
    ActorSystem(
      Behaviors.setup[SelfUp] { ctx =>
        val cluster = Cluster(ctx.system)
        cluster.subscriptions ! Subscribe(ctx.self, classOf[SelfUp])
        Behaviors.receiveMessage {
          case SelfUp(_) =>
            ctx.log.info("Node is up")
            if (cluster.selfMember.hasRole("back-end")) {
              WorkManagerSingleton.init(ctx.system)
            }
            if (cluster.selfMember.hasRole("front-end")) {
              val workManagerProxy = WorkManagerSingleton.init(ctx.system)
              ctx.spawn(FrontEnd(workManagerProxy), "front-end")
              ctx.spawn(WorkResultConsumer(), "consumer")
            }
            if (cluster.selfMember.hasRole("worker")) {
              (1 to workers).foreach(n => ctx.spawn(Worker(), s"worker-$n"))
            }
            Behaviors.same
        }
      },
      "ClusterSystem",
      config
    )

  def config(port: Int, role: String): Config =
    ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles=[$role]
    """).withFallback(ConfigFactory.load())

}
