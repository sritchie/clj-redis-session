(ns clj-redis-session.core
  "Redis session storage."
  (:import
   java.util.UUID)
  (:require
   [clj-redis.client :as r])
  (:use
   ring.middleware.session.store))

(deftype RedisStore [db key]
  SessionStore
  (read-session [_ session-key]
    (when session-key
      (when-let [data (r/hget db key session-key)]
        (read-string data))))
  (write-session [_ session-key data]
    (let [session-key (or session-key (str (UUID/randomUUID)))]
      (r/hset db key session-key (binding [*print-dup* true]
                                   (print-str data)))
      session-key))
  (delete-session [_ session-key]
    (r/hdel db key session-key)
    nil))

(defn redis-store
  "Creates a redis-backed session storage engine."
  ([db] (redis-store db "sessions"))
  ([db key] (RedisStore. db key)))
