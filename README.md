# lacinia-apollo-federation

A Clojure library allowing the creating of a schema which is compatible with Apollo's [federation spec](https://www.apollographql.com/docs/apollo-server/federation/federation-spec/).

## Usage

Imagine we have an toy movie schema:

```graphql
schema {
  query: Query
}

type Query {
  Movie: [Movie!]
}

type Movie @key(fields: "id") {
  id: ID!
  title: String!
}
```

We've already added the `@key` directive to the `Movie` type as-per the spec.

Start out with a `:require` of `com.atomist.lacinia-apollo-federation`:

```clojure
(ns my.app
  (:require [com.atomist.lacinia-apollo-federation :as federation]))
```

For each type the schema declares we'll need to register a `resolve-reference` (this is equivalent to Apollo's `__resolveReference` function). The following example registers a reference resolver for the `:Movie` type which we lookup by `:id`.

```clojure
(defmethod federation/resolve-reference :Movie
  [reference]
  (fetch-movie-by-id (:id reference)))
```

Finally create your federated schema. At the moment the library only takes the raw SDL string form of the schema so pass it into `build-federated-schema` along with any resolvers required for this schema. The library will create the necessary additional schema elements required by the [federation spec](https://www.apollographql.com/docs/apollo-server/federation/federation-spec/).

```clojure
(def compiled-schema
  (-> "schema.graphql"
      io/resource
      slurp
      (federation/build-federated-schema {:resolvers {:Query {:Movie fetch-all-movies}}})
      schema/compile))
```