import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Sidebar } from './components/Sidebar';
import { Home } from './pages/Home';
import { Habits } from './pages/Habits';
import { Goals } from './pages/Goals';
import { Challenges } from './pages/Challenges';
import { Profile } from './pages/Profile';
import { Analytics } from './pages/Analytics';
import { useAuth } from './auth/KeycloakContext';

function App() {
  const { isAuthenticated } = useAuth();

  return (
    <Router>
      <div className="flex min-h-screen bg-dark text-slate-100">
        <Sidebar />
        <main className="flex-1 p-8 max-w-4xl mx-auto w-full">
          {!isAuthenticated ? (
            <div className="flex flex-col items-center justify-center h-full text-center">
              <h1 className="text-4xl font-bold mb-4 bg-gradient-to-r from-accent-green to-accent-cyan bg-clip-text text-transparent">Welcome to HealthySocial</h1>
              <p className="text-slate-400 mb-8 text-lg">Please login to view your feed, track habits, and join challenges.</p>
              <Routes>
                 <Route path="*" element={<Navigate to="/" replace />} />
                 <Route path="/" element={<div/>} />
              </Routes>
            </div>
          ) : (
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/habits" element={<Habits />} />
              <Route path="/goals" element={<Goals />} />
              <Route path="/challenges" element={<Challenges />} />
              <Route path="/analytics" element={<Analytics />} />
              <Route path="/profile/:id" element={<Profile />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          )}
        </main>
      </div>
    </Router>
  );
}

export default App;
