(ns com.atomist.lacinia-apollo-federation
  (:require [com.walmartlabs.lacinia.parser.schema :as schema-parser]
            [com.walmartlabs.lacinia.schema :as schema]))

(defn- type-name
  [representation]
  (some-> representation :__typename keyword))

(defmulti resolve-reference
  type-name)

(defn- _entities
  [context {:keys [representations] :as arguments} value]
  (map (fn [reference]
         (some-> (resolve-reference reference)
                 (schema/tag-with-type (type-name reference))))
       representations))

(defn- uses-key-directive?
  [[object {:keys [directives]}]]
  (contains? (set (map :directive-type directives)) :key))

(defn build-federated-schema
  [schema-sdl attach]
  (let [parsed-schema (schema-parser/parse-schema schema-sdl attach)
        query-root (-> parsed-schema :roots :query)]
    (-> parsed-schema
        (assoc-in [:objects query-root :fields :_entities] {:type '(non-null (list :_Entity))
                                                            :args {:representations {:type '(non-null (list (non-null :_Any)))}}
                                                            :resolve _entities})
        (assoc-in [:objects query-root :fields :_service] {:type '(non-null :_Service)
                                                           :resolve (constantly {:sdl schema-sdl})})
        (assoc-in [:objects :_Service] {:fields {:sdl {:type 'String}}})
        (assoc-in [:scalars :_Any] {:parse identity
                                    :serialize identity})
        (assoc-in [:scalars :_FieldSet] {:parse identity
                                         :serialize identity})
        (assoc-in [:unions :_Entity] {:members (map first (filter uses-key-directive? (:objects parsed-schema)))})
        (assoc-in [:directive-defs :external] {:locations #{:field-definition}})
        (assoc-in [:directive-defs :requires] {:locations #{:field-definition}
                                               :args {:fields {:type '(non-null :_FieldSet)}}})
        (assoc-in [:directive-defs :provides] {:locations #{:field-definition}
                                               :args {:fields {:type '(non-null :_FieldSet)}}})
        (assoc-in [:directive-defs :key] {:locations #{:interface :object}
                                          :args {:fields {:type '(non-null :_FieldSet)}}})
        (assoc-in [:directive-defs :extends] {:locations #{:interface :object}}))))
