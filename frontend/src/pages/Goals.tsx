import { useState, useEffect } from 'react';
import { Target, Plus, Trash2 } from 'lucide-react';
import { api } from '../api/axios';

export function Goals() {
  const [goals, setGoals] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newGoal, setNewGoal] = useState({ title: '', description: '', category: 'FITNESS', targetDate: '' });

  const fetchGoals = async () => {
    try {
      const res = await api.get('/goals');
      setGoals(res.data.content || res.data || []);
    } catch (error) {
      console.error('Error fetching goals', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGoals();
  }, []);

  const handleCreate = async () => {
    if (!newGoal.title.trim() || !newGoal.targetDate) return;
    try {
      await api.post('/goals', newGoal);
      setShowCreate(false);
      setNewGoal({ title: '', description: '', category: 'FITNESS', targetDate: '' });
      fetchGoals();
    } catch (error) {
      console.error('Error creating goal', error);
    }
  };

  const handleProgressUpdate = async (goalId: string, currentPct: number) => {
    const next = Math.min(100, currentPct + 10);
    try {
      await api.patch(`/goals/${goalId}/progress`, { progressPercentage: next });
      fetchGoals();
    } catch (error) {
      console.error('Error updating progress', error);
    }
  };

  const handleDelete = async (goalId: string) => {
    try {
      await api.delete(`/goals/${goalId}`);
      fetchGoals();
    } catch (error) {
      console.error('Error deleting goal', error);
    }
  };

  return (
    <div className="animate-[fadeIn_0.4s_ease-out]">
      <header className="mb-8 flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-extrabold mb-1 bg-gradient-to-r from-accent-cyan to-blue-500 bg-clip-text text-transparent">My Goals</h1>
          <p className="text-slate-400 text-lg">Set targets and crush them</p>
        </div>
        <button onClick={() => setShowCreate(!showCreate)} className="btn-primary flex items-center gap-2 cursor-pointer">
          <Plus size={20} />
          New Goal
        </button>
      </header>

      {showCreate && (
        <div className="glass-card mb-8 border-accent-cyan/30">
          <h3 className="text-xl font-bold mb-4 text-white">Create New Goal</h3>
          <div className="grid grid-cols-2 gap-4 mb-4">
            <input 
              type="text" 
              placeholder="Goal Title" 
              className="bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-accent-cyan"
              value={newGoal.title}
              onChange={e => setNewGoal({...newGoal, title: e.target.value})}
            />
            <select 
              className="bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-accent-cyan appearance-none"
              value={newGoal.category}
              onChange={e => setNewGoal({...newGoal, category: e.target.value})}
            >
              <option value="FITNESS">Fitness</option>
              <option value="NUTRITION">Nutrition</option>
              <option value="MENTAL_HEALTH">Mental Health</option>
              <option value="SLEEP">Sleep</option>
              <option value="HYDRATION">Hydration</option>
              <option value="WEIGHT">Weight</option>
              <option value="ENDURANCE">Endurance</option>
              <option value="STRENGTH">Strength</option>
              <option value="FLEXIBILITY">Flexibility</option>
              <option value="OTHER">Other</option>
            </select>
            <input 
              type="date" 
              className="col-span-2 bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-accent-cyan"
              value={newGoal.targetDate}
              onChange={e => setNewGoal({...newGoal, targetDate: e.target.value})}
            />
          </div>
          <div className="flex justify-end gap-2">
            <button onClick={() => setShowCreate(false)} className="px-4 py-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors cursor-pointer">Cancel</button>
            <button onClick={handleCreate} className="px-4 py-2 rounded-lg bg-accent-cyan text-dark font-semibold hover:bg-cyan-400 transition-colors cursor-pointer">Save Goal</button>
          </div>
        </div>
      )}

      <div className="grid gap-6">
        {loading ? (
          <div className="text-center text-slate-400 py-8">Loading goals...</div>
        ) : goals.length === 0 ? (
          <div className="text-center text-slate-400 glass-card py-12">No active goals. Set one to stay focused!</div>
        ) : (
          goals.map(goal => {
            const percent = goal.progressPercentage || 0;
            return (
              <div key={goal.id} className="glass-card relative group overflow-hidden">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex items-center gap-3">
                    <div className="p-3 rounded-xl bg-accent-cyan/20 text-accent-cyan">
                      <Target size={24} />
                    </div>
                    <div>
                      <h3 className="text-xl font-bold text-slate-100">{goal.title}</h3>
                       <p className="text-sm text-slate-400">Target: {new Date(goal.targetDate).toLocaleDateString()} · Category: {goal.category}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button onClick={() => handleProgressUpdate(goal.id, goal.progressPercentage || 0)} className="p-2 bg-white/5 hover:bg-white/10 rounded-lg text-slate-300 transition-colors cursor-pointer">  
                      +10%
                    </button>
                    <button onClick={() => handleDelete(goal.id)} className="p-2 text-slate-500 hover:text-red-400 transition-colors cursor-pointer">
                      <Trash2 size={20} />
                    </button>
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-sm mb-1 font-semibold">
                    <span className="text-accent-cyan">{percent}%</span>
                    <span className="text-slate-400">{goal.progressPercentage || 0}% complete</span>
                  </div>
                  <div className="w-full h-3 bg-black/30 rounded-full overflow-hidden border border-white/5">
                    <div className="h-full bg-gradient-to-r from-accent-cyan to-blue-500 transition-all duration-500 ease-out" style={{ width: `${percent}%` }}></div>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
