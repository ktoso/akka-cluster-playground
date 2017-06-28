package playground;

import akka.actor.*;
import akka.cluster.routing.ClusterRouterGroup;
import akka.cluster.routing.ClusterRouterGroupSettings;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.FromConfig;
import akka.routing.RoundRobinGroup;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.aeron.driver.Sender;

import java.util.Collections;

public class Main {
  public static void main(String[] args) {
    final ActorSystem s1 = system(2551);
    final ActorSystem s2 = system(2552);
    final ActorSystem s3 = system(2553);

    ActorRef l = s1.actorOf(Props.create(LoggingOnly.class), "logging");
    s1.actorOf(Props.create(EchoActor.class), "preorder_cancelPending");
    s2.actorOf(FromConfig.getInstance().props(Props.create(EchoActor.class)), "preorder_cancelPending");
    s3.actorOf(FromConfig.getInstance().props(Props.create(EchoActor.class)), "preorder_cancelPending");
    
    final String role = null;
    final ActorRef ref = getClusterActorRef(s1, "cancelPending", role);

    sleep();
    sleep();
    sleep();
    
    for (int i = 0; i < 100; i++) {
      ref.tell("hello", l);
    }


    System.console().readLine();
    
  }

  private static ActorSystem system(int port) {
    final Config conf = ConfigFactory.parseString("\n" +
      "akka.remote.netty.tcp.hostname = \"127.0.0.1\"\n" +
      "akka.remote.netty.tcp.port = " + port + "\n" +
      "").withFallback(ConfigFactory.load());

    ActorSystem system = ActorSystem.create("app", conf);
    sleep();
    return system;
  }

  private static void sleep() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
  }

  static final ActorRef getClusterActorRef(ActorSystem system, String actorRoute, String actorRole) {
    if (!actorRoute.startsWith("/user/preorder_")) {
      actorRoute = "/user/preorder_" + actorRoute;
    }
    Iterable<String> actorRouteesPaths = Collections.singletonList(actorRoute);
    return system.actorOf(
      new ClusterRouterGroup(new RoundRobinGroup(actorRouteesPaths),
        new ClusterRouterGroupSettings(10000, actorRouteesPaths,
          false, actorRole)).props());

  }
}

class EchoActor extends AbstractLoggingActor {

  @Override
  public void preStart() throws Exception {
    log().info("Started {}", self().path());
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create()
      .matchAny(msg -> sender() .tell(msg, self()))
      .build();
  }
}
class LoggingOnly extends AbstractLoggingActor {

  @Override
  public void preStart() throws Exception {
    log().info("Started {}", self().path());
  }

  @Override
  public Receive createReceive() {
    return ReceiveBuilder.create()
      .matchAny(msg -> {
        log().info("GOT: {} from {} ", msg, sender());
      })
      .build();
  }
}
