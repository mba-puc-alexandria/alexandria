'use client';

import { GoogleOAuthProvider } from '@react-oauth/google';
import { ReactNode } from 'react';

export default function GoogleProvider({ children }: { children: ReactNode }) {
  return (
    <GoogleOAuthProvider clientId="585326352858-p23r7po387fmga55vbv5hjqf26ah35db.apps.googleusercontent.com">
      {children}
    </GoogleOAuthProvider>
  );
}
