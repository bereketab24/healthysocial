import { useEffect, useState } from 'react';
import { api } from '../api/axios';

type TopUser = {
  username: string;
  postCount: number;
  commentCount: number;
  likesReceived: number;
  activityScore: number;
};

type EngagementDay = {
  day: string;
  posts: number;
  comments: number;
  likes: number;
};

type ChallengeStat = {
  title: string;
  category: string;
  participants: number;
  completed: number;
  completionRate: number;
};

type HabitStreak = {
  username: string;
  habitName: string;
  completedLogs: number;
};

type PostEngagement = {
  postId: string;
  authorUsername: string;
  preview: string;
  likes: number;
  comments: number;
  engagement: number;
};

const maxOf = (values: number[]) => Math.max(1, ...values);

export function Analytics() {
  const [topUsers, setTopUsers] = useState<TopUser[]>([]);
  const [engagement, setEngagement] = useState<EngagementDay[]>([]);
  const [challenges, setChallenges] = useState<ChallengeStat[]>([]);
  const [streaks, setStreaks] = useState<HabitStreak[]>([]);
  const [posts, setPosts] = useState<PostEngagement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        const [tu, en, ch, hs, pe] = await Promise.all([
          api.get<TopUser[]>('/analytics/top-users?limit=10'),
          api.get<EngagementDay[]>('/analytics/engagement?days=14'),
          api.get<ChallengeStat[]>('/analytics/challenge-stats?limit=8'),
          api.get<HabitStreak[]>('/analytics/habit-streaks?limit=10'),
          api.get<PostEngagement[]>('/analytics/post-engagement?limit=8'),
        ]);
        setTopUsers(tu.data);
        setEngagement(en.data);
        setChallenges(ch.data);
        setStreaks(hs.data);
        setPosts(pe.data);
      } catch (err) {
        console.error('Failed to load analytics', err);
        setError('Could not load analytics. Make sure the BigQuery pipeline is configured.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const engagementMax = maxOf(engagement.flatMap(d => [d.posts, d.comments, d.likes]));

  return (
    <div className="animate-[fadeIn_0.4s_ease-out]">
      <header className="mb-8">
        <h1 className="text-4xl font-extrabold mb-1 bg-gradient-to-r from-accent-green to-accent-cyan bg-clip-text text-transparent">
          Analytics
        </h1>
        <p className="text-slate-400 text-lg">Powered by BigQuery — live community insights</p>
      </header>

      {loading && <div className="glass-card text-center text-slate-400 py-12">Crunching numbers…</div>}

      {error && !loading && (
        <div className="glass-card text-center text-amber-400 py-8">{error}</div>
      )}

      {!loading && !error && (
        <div className="flex flex-col gap-8">
          {/* Top users */}
          <section className="glass-card">
            <h2 className="text-xl font-bold mb-4 text-slate-100">Most active members</h2>
            {topUsers.length === 0 ? (
              <p className="text-slate-500 text-sm">No data yet.</p>
            ) : (
              <ul className="flex flex-col gap-3">
                {topUsers.map((u, idx) => {
                  const max = maxOf(topUsers.map(x => x.activityScore));
                  const pct = (u.activityScore / max) * 100;
                  return (
                    <li key={u.username} className="flex flex-col gap-1">
                      <div className="flex justify-between text-sm">
                        <span className="text-slate-300">
                          <span className="text-slate-500 mr-2">#{idx + 1}</span>
                          {u.username}
                        </span>
                        <span className="text-slate-400">
                          {u.postCount} posts · {u.commentCount} comments · {u.likesReceived} likes
                        </span>
                      </div>
                      <div className="h-2 bg-white/5 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-accent-green to-accent-cyan rounded-full"
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </li>
                  );
                })}
              </ul>
            )}
          </section>

          {/* Engagement timeline */}
          <section className="glass-card">
            <h2 className="text-xl font-bold mb-4 text-slate-100">Last 14 days of engagement</h2>
            {engagement.length === 0 ? (
              <p className="text-slate-500 text-sm">No data yet.</p>
            ) : (
              <div className="flex items-end gap-2 h-48">
                {engagement.map(d => {
                  const total = d.posts + d.comments + d.likes;
                  const h = (total / engagementMax) * 100;
                  return (
                    <div key={d.day} className="flex-1 flex flex-col items-center gap-2">
                      <div
                        className="w-full bg-gradient-to-t from-accent-green to-accent-cyan rounded-t"
                        style={{ height: `${Math.max(h, 4)}%` }}
                        title={`${d.day}: ${d.posts} posts / ${d.comments} comments / ${d.likes} likes`}
                      />
                      <span className="text-[10px] text-slate-500 rotate-45 origin-top-left whitespace-nowrap">
                        {d.day.slice(5)}
                      </span>
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          {/* Challenges */}
          <section className="glass-card">
            <h2 className="text-xl font-bold mb-4 text-slate-100">Challenge completion</h2>
            {challenges.length === 0 ? (
              <p className="text-slate-500 text-sm">No data yet.</p>
            ) : (
              <ul className="flex flex-col gap-3">
                {challenges.map(c => (
                  <li key={c.title} className="flex flex-col gap-1">
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-300">
                        {c.title}
                        <span className="text-slate-500 ml-2 text-xs">{c.category}</span>
                      </span>
                      <span className="text-slate-400">
                        {c.completed}/{c.participants} ({Math.round(c.completionRate * 100)}%)
                      </span>
                    </div>
                    <div className="h-2 bg-white/5 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-accent-cyan rounded-full"
                        style={{ width: `${c.completionRate * 100}%` }}
                      />
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>

          {/* Two-column row: habit streaks + posts */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <section className="glass-card">
              <h2 className="text-xl font-bold mb-4 text-slate-100">Top habit logs</h2>
              {streaks.length === 0 ? (
                <p className="text-slate-500 text-sm">No data yet.</p>
              ) : (
                <ul className="flex flex-col gap-2">
                  {streaks.map((s, i) => (
                    <li key={i} className="flex justify-between text-sm border-b border-white/5 pb-2">
                      <span className="text-slate-300">
                        {s.username} · <span className="text-slate-400">{s.habitName}</span>
                      </span>
                      <span className="text-accent-green font-semibold">{s.completedLogs}</span>
                    </li>
                  ))}
                </ul>
              )}
            </section>

            <section className="glass-card">
              <h2 className="text-xl font-bold mb-4 text-slate-100">Top posts</h2>
              {posts.length === 0 ? (
                <p className="text-slate-500 text-sm">No data yet.</p>
              ) : (
                <ul className="flex flex-col gap-3">
                  {posts.map(p => (
                    <li key={p.postId} className="border-b border-white/5 pb-2 last:border-b-0">
                      <p className="text-sm text-slate-300 truncate">{p.preview}</p>
                      <div className="flex justify-between text-xs text-slate-500 mt-1">
                        <span>by {p.authorUsername}</span>
                        <span>
                          {p.likes} ♥ · {p.comments} 💬
                        </span>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </div>
        </div>
      )}
    </div>
  );
}
