package cluster;

import java.util.Arrays;
import java.util.List;

import akka.management.javadsl.AkkaManagement;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.typed.ActorSystem;

public class Runner {
    public static void main(String[] args) {
        if (args.length == 0) {
            startupClusterNodes(Arrays.asList("2551", "2552", "0"));
        } else {
            startupClusterNodes(Arrays.asList(args));
        }
    }

    private static void startupClusterNodes(List<String> ports) {
        System.out.printf("Start cluster on port(s) %s%n", ports);

        ports.forEach(port -> {
            final ActorSystem<Void> actorSystem = ActorSystem.create(Main.create(), "cluster", setupClusterNodeConfig(port));
            AkkaManagement.get(actorSystem.classicSystem()).start();
            HttpServer.start(actorSystem);
        });
    }

    private static Config setupClusterNodeConfig(String port) {
        final String hostname = "127.0.0.1";
        return ConfigFactory
                .parseString(String.format("akka.remote.artery.canonical.hostname = \"%s\"%n", hostname)
                        + String.format("akka.remote.artery.canonical.port=%s%n", port)
                        + String.format("akka.management.http.hostname = \"%s\"%n", "127.0.0.1")
                        + String.format("akka.management.http.port=%s%n", port.replace("255", "855"))
                        + String.format("akka.management.http.route-providers-read-only = %s%n", "false")
                        + String.format("akka.remote.artery.advanced.tcp.outbound-client-hostname = %s%n", hostname))
                .withFallback(ConfigFactory.load());
    }
}
