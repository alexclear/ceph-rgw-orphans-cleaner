#!/usr/bin/env bb

(require '[babashka.process :refer [shell]]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

;; Configuration
(def pool-name "ceph-objectstore.rgw.buckets.data")

;; Parse command line arguments for batch size (default 10)
(def batch-size 
  (if-let [batch-arg (first (filter #(str/starts-with? % "--batch=") *command-line-args*))]
    (Integer/parseInt (second (str/split batch-arg #"=")))
    10))

;; Change directory to /tmp
(System/setProperty "user.dir" "/tmp")

;; Get the input file containing the list of orphaned objects
(def output (:out (shell {:out :string} "rgw-orphan-list" pool-name)))
(def input-file 
  (second (re-find #"The results can be found in '([^']+)'" output)))

(println "Processing orphans from file:" input-file)
(println "Using batch size:" batch-size)

;; Function to delete an object
(defn delete-object [object]
  (let [result (shell {:out :string :err :string :continue true}
                      "rados" "-p" pool-name "rm" object)]
    (if (zero? (:exit result))
      (println "Successfully deleted:" object)
      (binding [*out* *err*]
        (println "Failed to delete:" object)
        (println "Error message:" (:err result))))))

;; Read file and process in batches
(with-open [reader (io/reader input-file)]
  (let [objects (filter #(not (str/blank? %)) (line-seq reader))]
    (doseq [batch (partition-all batch-size objects)]
      (println "Processing batch of" (count batch) "objects...")
      ;; Replace sequential doseq with parallel processing
      (doall (pmap delete-object batch))
      (println "Batch completed"))))

(println "Deletion process completed")
