(defproject org.clojars.automata/rosado.processing "1.1.0"
            :description "A Live Programming Music REPL using processing"
            :url "http://mad.emotionull.com"
            :dependencies [[org.clojure/clojure "1.2.0"]
                           [org.clojars.technomancy/rosado.processing "1.1.0"]
                           [org.clojars.automata/ddf.minim "2.1.0"]]
            :dev-dependencies [[lein-clojars "0.5.0-SNAPSHOT"]
                               [swank-clojure "1.2.0"]]
            :main music-as-data.playground
            :jvm-opts ["-Xms256m" "-Xmx1g" "-XX:+UseConcMarkSweepGC" "-server"])

