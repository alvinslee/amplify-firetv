import { defineBackend } from '@aws-amplify/backend';
import { auth } from './auth/resource';
import { storage } from './storage/resource';
import { data } from './data/resource';
/**
 * @see https://docs.amplify.aws/android/build-a-backend/storage/set-up-storage/
 */
defineBackend({
  auth,
  storage,
  data,
});
