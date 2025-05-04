import { type ClientSchema, a, defineData } from '@aws-amplify/backend';

const schema = a.schema({
  Favorite: a
    .model({
      userId: a.string().required(),
      videoId: a.string().required()
    })
    .authorization(allow => [
      allow.authenticated()
    ]),
});

export type Schema = ClientSchema<typeof schema>;

export const data = defineData({
  schema,
  authorizationModes: {
    defaultAuthorizationMode: 'userPool'
  }
});
