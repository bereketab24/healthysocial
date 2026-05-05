import { useState, useEffect } from 'react';
import { Check, Plus, Trash2 } from 'lucide-react';
import { api } from '../api/axios';

export function Habits() {
  const [habits, setHabits] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newHabit, setNewHabit] = useState({ name: '', description: '', frequency: 'DAILY', category: 'EXERCISE', targetCount: 1 });

  const fetchHabits = async () => {
    try {
      const res = await api.get('/habits');
      setHabits(res.data.content || res.data || []);
    } catch (error) {
      console.error('Error fetching habits', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHabits();
  }, []);

  const handleCreate = async () => {
    if (!newHabit.name.trim()) return;
    try {
      await api.post('/habits', newHabit);
      setShowCreate(false);
      setNewHabit({ name: '', description: '', frequency: 'DAILY', category: 'EXERCISE', targetCount: 1 });
      fetchHabits();
    } catch (error) {
      console.error('Error creating habit', error);
    }
  };

  const handleLog = async (habitId: string) => {
    try {
      await api.post('/habits/logs', { habitId, note: 'Logged from UI' });
      fetchHabits();
    } catch (error) {
      console.error('Error logging habit', error);
    }
  };

  const handleDelete = async (habitId: string) => {
    try {
      await api.delete(`/habits/${habitId}`);
      fetchHabits();
    } catch (error) {
      console.error('Error deleting habit', error);
    }
  };

  return (
    <div className="animate-[fadeIn_0.4s_ease-out]">
      <header className="mb-8 flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-extrabold mb-1 bg-gradient-to-r from-accent-green to-accent-cyan bg-clip-text text-transparent">My Habits</h1>
          <p className="text-slate-400 text-lg">Build consistency every day</p>
        </div>
        <button onClick={() => setShowCreate(!showCreate)} className="btn-primary flex items-center gap-2 cursor-pointer">
          <Plus size={20} />
          New Habit
        </button>
      </header>

      {showCreate && (
        <div className="glass-card mb-8 border-accent-cyan/30">
          <h3 className="text-xl font-bold mb-4 text-white">Create New Habit</h3>
          <div className="grid grid-cols-2 gap-4 mb-4">
            <input 
              type="text" 
              placeholder="Habit Name (e.g. Drink Water)" 
              className="bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-accent-cyan"
              value={newHabit.name}
              onChange={e => setNewHabit({...newHabit, name: e.target.value})}
            />
            <select 
              className="bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-accent-cyan appearance-none"
              value={newHabit.category}
              onChange={e => setNewHabit({...newHabit, category: e.target.value})}
            >
              <option value="EXERCISE">Exercise</option>
              <option value="NUTRITION">Nutrition</option>
              <option value="SLEEP">Sleep</option>
              <option value="MINDFULNESS">Mindfulness</option>
              <option value="HYDRATION">Hydration</option>
              <option value="SOCIAL">Social</option>
              <option value="LEARNING">Learning</option>
              <option value="OTHER">Other</option>
            </select>
            <input 
              type="text" 
              placeholder="Description (Optional)" 
              className="col-span-2 bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-accent-cyan"
              value={newHabit.description}
              onChange={e => setNewHabit({...newHabit, description: e.target.value})}
            />
          </div>
          <div className="flex justify-end gap-2">
            <button onClick={() => setShowCreate(false)} className="px-4 py-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors cursor-pointer">Cancel</button>
            <button onClick={handleCreate} className="px-4 py-2 rounded-lg bg-accent-cyan text-dark font-semibold hover:bg-cyan-400 transition-colors cursor-pointer">Create Habit</button>
          </div>
        </div>
      )}

      <div className="grid gap-4">
        {loading ? (
          <div className="text-center text-slate-400 py-8">Loading habits...</div>
        ) : habits.length === 0 ? (
          <div className="text-center text-slate-400 glass-card py-12">No active habits. Create one to get started!</div>
        ) : (
          habits.map(habit => (
            <div key={habit.id} className="glass-card flex items-center justify-between group">
              <div className="flex items-center gap-6">
                <button 
                  onClick={() => handleLog(habit.id)}
                  className={`w-14 h-14 rounded-full border-2 flex items-center justify-center transition-all duration-300 cursor-pointer ${habit.completedToday ? 'bg-accent-green border-accent-green text-white shadow-[0_0_15px_rgba(16,185,129,0.4)]' : 'border-white/20 text-transparent hover:border-accent-green hover:text-white/20'}`}
                >
                  <Check size={32} />
                </button>
                <div>
                  <h3 className="text-xl font-bold text-slate-100 mb-1">{habit.name}</h3>
                  <div className="flex gap-4 text-sm font-medium">
                    <span className="flex items-center gap-1 text-accent-cyan">
                      🔥 {habit.currentStreak || 0} day streak
                    </span>
                    <span className="text-slate-400 bg-white/5 px-2 rounded-md uppercase text-xs flex items-center">{habit.category}</span>
                  </div>
                </div>
              </div>
              <button onClick={() => handleDelete(habit.id)} className="text-slate-500 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-opacity p-2 cursor-pointer">
                <Trash2 size={20} />
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
