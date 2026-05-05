import { NavLink } from 'react-router-dom';
import { Home, CheckCircle, Trophy, User, LogIn, LogOut } from 'lucide-react';
import { useAuth } from '../auth/KeycloakContext';

export function Sidebar() {
  const { isAuthenticated, login, logout, user } = useAuth();

  return (
    <aside className="w-64 glass-card border-l-0 border-t-0 border-b-0 rounded-none flex flex-col sticky top-0 h-screen p-6">
      <div className="flex items-center gap-3 mb-12 text-accent-green">
        <span className="text-2xl">✨</span>
        <h2 className="text-xl font-bold text-slate-100">HealthySocial</h2>
      </div>
      
      <nav className="flex flex-col gap-2 flex-1">
        {isAuthenticated && (
          <>
            <NavLink to="/" className={({ isActive }) => `flex items-center gap-4 p-3 rounded-xl font-medium transition-all duration-200 ${isActive ? 'bg-accent-green/10 text-accent-green' : 'text-slate-400 hover:bg-white/5 hover:text-slate-100'}`}>
              <Home size={20} />
              <span>Feed</span>
            </NavLink>
            <NavLink to="/habits" className={({ isActive }) => `flex items-center gap-4 p-3 rounded-xl font-medium transition-all duration-200 ${isActive ? 'bg-accent-green/10 text-accent-green' : 'text-slate-400 hover:bg-white/5 hover:text-slate-100'}`}>
              <CheckCircle size={20} />
              <span>Habits</span>
            </NavLink>
            <NavLink to="/goals" className={({ isActive }) => `flex items-center gap-4 p-3 rounded-xl font-medium transition-all duration-200 ${isActive ? 'bg-accent-green/10 text-accent-green' : 'text-slate-400 hover:bg-white/5 hover:text-slate-100'}`}>
              <Trophy size={20} />
              <span>Goals</span>
            </NavLink>
            <NavLink to="/challenges" className={({ isActive }) => `flex items-center gap-4 p-3 rounded-xl font-medium transition-all duration-200 ${isActive ? 'bg-accent-green/10 text-accent-green' : 'text-slate-400 hover:bg-white/5 hover:text-slate-100'}`}>
              <Trophy size={20} />
              <span>Challenges</span>
            </NavLink>
            <NavLink to="/profile/me" className={({ isActive }) => `flex items-center gap-4 p-3 rounded-xl font-medium transition-all duration-200 ${isActive ? 'bg-accent-green/10 text-accent-green' : 'text-slate-400 hover:bg-white/5 hover:text-slate-100'}`}>
              <User size={20} />
              <span>Profile</span>
            </NavLink>
          </>
        )}
      </nav>

      <div className="mt-auto border-t border-white/10 pt-6">
        {isAuthenticated ? (
          <div className="flex flex-col gap-4">
            <div className="flex items-center gap-3 px-3">
              <div className="w-10 h-10 rounded-full bg-accent-cyan/20 border border-accent-cyan flex items-center justify-center font-bold text-accent-cyan uppercase">
                {user?.username?.charAt(0) || 'U'}
              </div>
              <div className="flex flex-col overflow-hidden">
                <span className="text-sm font-semibold truncate">{user?.username || 'User'}</span>
              </div>
            </div>
            <button onClick={logout} className="flex items-center gap-4 p-3 rounded-xl font-medium text-slate-400 hover:bg-red-500/10 hover:text-red-400 transition-all duration-200 w-full text-left cursor-pointer">
              <LogOut size={20} />
              <span>Logout</span>
            </button>
          </div>
        ) : (
          <button onClick={login} className="flex items-center gap-4 p-3 rounded-xl font-medium bg-accent-green/20 hover:bg-accent-green/30 text-accent-green transition-all duration-200 w-full text-left cursor-pointer">
            <LogIn size={20} />
            <span>Login</span>
          </button>
        )}
      </div>
    </aside>
  );
}
