import { useState, useEffect } from 'react';
import { Trophy, Users, Plus, CheckCircle, Trash2, ShieldX } from 'lucide-react';
import { api } from '../api/axios';
import { useAuth } from '../auth/KeycloakContext';

export function Challenges() {
  const { user } = useAuth();
  const [challenges, setChallenges] = useState<any[]>([]);
  const [leaderboard, setLeaderboard] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newChallenge, setNewChallenge] = useState({ title: '', description: '', startDate: '', endDate: '', category: 'EXERCISE' });

  const fetchData = async () => {
    try {
      const [chalRes, leadRes] = await Promise.all([
        api.get('/challenges'),
        api.get('/leaderboard')
      ]);
      setChallenges(chalRes.data.content || chalRes.data || []);
      setLeaderboard(leadRes.data.content || leadRes.data || []);
    } catch (error) {
      console.error('Error fetching challenges data', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreate = async () => {
    if (!newChallenge.title.trim()) return;
    try {
      await api.post('/challenges', newChallenge);
      setShowCreate(false);
      setNewChallenge({ title: '', description: '', startDate: '', endDate: '', category: 'EXERCISE' });
      fetchData();
    } catch (error) {
      console.error('Error creating challenge', error);
    }
  };

  const handleJoin = async (id: string) => {
    try {
      await api.post(`/challenges/${id}/join`);
      fetchData();
    } catch (error) {
      console.error('Error joining challenge', error);
    }
  };

  const handleLeave = async (id: string) => {
    try {
      await api.delete(`/challenges/${id}/join`);
      fetchData();
    } catch (error) {
      console.error('Error leaving challenge', error);
    }
  };

  const handleComplete = async (id: string) => {
    try {
      await api.post(`/challenges/${id}/complete`);
      fetchData();
    } catch (error) {
      console.error('Error completing challenge', error);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await api.delete(`/challenges/${id}`);
      fetchData();
    } catch (error) {
      console.error('Error deleting challenge', error);
    }
  };

  return (
    <div className="animate-[fadeIn_0.4s_ease-out]">
      <header className="mb-8 flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-extrabold mb-1 bg-gradient-to-r from-yellow-400 to-orange-500 bg-clip-text text-transparent">Challenges</h1>
          <p className="text-slate-400 text-lg">Push your limits with the community</p>
        </div>
        <button onClick={() => setShowCreate(!showCreate)} className="btn-primary flex items-center gap-2 cursor-pointer bg-gradient-to-r from-yellow-500 to-orange-500 shadow-[0_4px_14px_rgba(234,179,8,0.4)] hover:shadow-[0_6px_20px_rgba(234,179,8,0.4)]">
          <Plus size={20} />
          Create Challenge
        </button>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 flex flex-col gap-6">
          {showCreate && (
            <div className="glass-card border-yellow-500/30">
              <h3 className="text-xl font-bold mb-4 text-white">New Challenge</h3>
              <div className="grid grid-cols-2 gap-4 mb-4">
                <input 
                  type="text" 
                  placeholder="Challenge Title" 
                  className="col-span-2 bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-yellow-500"
                  value={newChallenge.title}
                  onChange={e => setNewChallenge({...newChallenge, title: e.target.value})}
                />
                <input 
                  type="date" 
                  className="bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-yellow-500"
                  value={newChallenge.startDate}
                  onChange={e => setNewChallenge({...newChallenge, startDate: e.target.value})}
                />
                <input 
                  type="date" 
                  className="bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-yellow-500"
                  value={newChallenge.endDate}
                  onChange={e => setNewChallenge({...newChallenge, endDate: e.target.value})}
                />
                <textarea 
                  placeholder="Description" 
                  className="col-span-2 bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-yellow-500"
                  rows={2}
                  value={newChallenge.description}
                  onChange={e => setNewChallenge({...newChallenge, description: e.target.value})}
                />
              </div>
              <div className="flex justify-end gap-2">
                <button onClick={() => setShowCreate(false)} className="px-4 py-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors cursor-pointer">Cancel</button>
                <button onClick={handleCreate} className="px-4 py-2 rounded-lg bg-yellow-500 text-dark font-semibold hover:bg-yellow-400 transition-colors cursor-pointer">Create</button>
              </div>
            </div>
          )}

          <h2 className="text-2xl font-bold text-slate-100 mb-2">Active Challenges</h2>
          {loading ? (
            <div className="text-center text-slate-400 py-8">Loading challenges...</div>
          ) : challenges.length === 0 ? (
            <div className="text-center text-slate-400 glass-card py-12">No challenges currently active. Create one!</div>
          ) : (
            challenges.map(challenge => {
              const isParticipating = challenge.isJoined;
              const isCreator = challenge.creatorId === user?.sub;

              return (
                <div key={challenge.id} className="glass-card relative overflow-hidden group">
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex items-start gap-4">
                      <div className="p-3 rounded-xl bg-yellow-500/20 text-yellow-500">
                        <Trophy size={28} />
                      </div>
                      <div>
                        <h3 className="text-xl font-bold text-slate-100 mb-1">{challenge.title}</h3>
                        <p className="text-slate-300 text-sm mb-2">{challenge.description}</p>
                        <div className="flex items-center gap-4 text-xs font-medium text-slate-400">
                          <span className="flex items-center gap-1"><Users size={14} /> {challenge.participantsCount || 0} participants</span>
                          <span>Ends: {new Date(challenge.endDate).toLocaleDateString()}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="flex gap-2 mt-4 pt-4 border-t border-white/5">
                    {!isParticipating ? (
                      <button onClick={() => handleJoin(challenge.id)} className="flex-1 py-2 rounded-lg bg-yellow-500/20 text-yellow-500 font-semibold hover:bg-yellow-500 hover:text-dark transition-colors cursor-pointer">
                        Join Challenge
                      </button>
                    ) : (
                      <>
                        <button onClick={() => handleComplete(challenge.id)} className="flex-1 py-2 rounded-lg bg-accent-green/20 text-accent-green font-semibold hover:bg-accent-green hover:text-dark transition-colors flex items-center justify-center gap-2 cursor-pointer">
                          <CheckCircle size={18} /> Complete
                        </button>
                        <button onClick={() => handleLeave(challenge.id)} className="px-4 py-2 rounded-lg bg-white/5 text-slate-300 hover:bg-white/10 transition-colors flex items-center justify-center gap-2 cursor-pointer">
                          <ShieldX size={18} /> Leave
                        </button>
                      </>
                    )}
                    {isCreator && (
                      <button onClick={() => handleDelete(challenge.id)} className="px-4 py-2 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500 hover:text-white transition-colors cursor-pointer">
                        <Trash2 size={18} />
                      </button>
                    )}
                  </div>
                </div>
              )
            })
          )}
        </div>

        <div className="lg:col-span-1">
          <div className="glass-card sticky top-8">
            <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
              <Trophy className="text-yellow-500" />
              Global Leaderboard
            </h3>
            
            <div className="flex flex-col gap-4">
              {leaderboard.length === 0 ? (
                <p className="text-slate-400 text-sm text-center py-4">No users on leaderboard yet.</p>
              ) : (
                leaderboard.map((userStats, index) => (
                  <div key={userStats.userId} className={`flex items-center gap-3 p-3 rounded-xl ${index === 0 ? 'bg-yellow-500/10 border border-yellow-500/30' : 'bg-white/5'}`}>
                    <div className={`w-8 h-8 flex items-center justify-center font-bold rounded-full ${index === 0 ? 'bg-yellow-500 text-dark' : index === 1 ? 'bg-slate-300 text-dark' : index === 2 ? 'bg-orange-400 text-dark' : 'bg-white/10 text-slate-300'}`}>
                      {index + 1}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-slate-100 truncate">{userStats.username}</p>
                      <p className="text-xs text-slate-400">🔥 {userStats.currentStreak || 0} streak · {userStats.totalHabitsCompleted || 0} habits</p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
