(ns clj-redis-session.core
  "Redis session storage."
  (:use ring.middleware.session.store)
  (:require [clj-redis.client :as r])
  (:import java.util.UUID))

(deftype RedisStore [db expiration]
  SessionStore
  (read-session [_ session-key]
    (when session-key
      (when-let [data (r/get db session-key)]
        (read-string data))))
  (write-session [_ session-key data]
    (let [session-key (or session-key (str (UUID/randomUUID)))
          data-str (binding [*print-dup* true]
                     (print-str data))]
      (if expiration
        (r/setex db session-key data-str expiration)
        (r/set db session-key data-str))
      session-key))
  (delete-session [_ session-key]
    (r/del db [session-key])
    nil))

(defn redis-store
  "Creates a redis-backed session storage engine."
  ([db] (redis-store db nil))
  ([db expiration-secs]
     (RedisStore. db expiration-secs)))
