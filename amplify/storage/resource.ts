import { defineStorage } from '@aws-amplify/backend';

export const storage = defineStorage({
  name: 'my-bucket',
  access: (allow) => ({
    'data/*': [
      allow.guest.to(['get']),
      allow.authenticated.to(['get'])
    ]
  })
}); 
