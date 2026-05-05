import { createContext, useContext, useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { keycloak } from './keycloak';

interface AuthContextType {
  isAuthenticated: boolean;
  login: () => void;
  logout: () => void;
  user: any;
}

const AuthContext = createContext<AuthContextType>({
  isAuthenticated: false,
  login: () => {},
  logout: () => {},
  user: null,
});

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<any>(null);
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    keycloak.init({ onLoad: 'check-sso' })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
        if (authenticated) {
          keycloak.loadUserProfile().then(profile => {
            setUser(profile);
          });
        }
        setInitialized(true);
      })
      .catch(console.error);

    keycloak.onAuthSuccess = () => {
      setIsAuthenticated(true);
      keycloak.loadUserProfile().then(profile => setUser(profile));
    };

    keycloak.onAuthLogout = () => {
      setIsAuthenticated(false);
      setUser(null);
    };
  }, []);

  const login = () => keycloak.login();
  const logout = () => keycloak.logout();

  if (!initialized) {
    return <div className="min-h-screen flex items-center justify-center bg-dark text-white">Loading...</div>;
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout, user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
