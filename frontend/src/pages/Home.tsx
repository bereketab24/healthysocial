import { useState, useEffect } from 'react';
import { api } from '../api/axios';
import { Post } from '../components/Post';

export function Home() {
  const [posts, setPosts] = useState<any[]>([]);
  const [newPost, setNewPost] = useState('');
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'feed' | 'discover'>('feed');

  const fetchFeed = async () => {
    try {
      const endpoint = activeTab === 'feed' ? '/posts/feed' : '/posts';
      const res = await api.get(endpoint); 
      setPosts(res.data.content || res.data || []);
    } catch (error) {
      console.error('Error fetching feed', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setLoading(true);
    fetchFeed();
  }, [activeTab]);

  const handlePost = async () => {
    if (!newPost.trim()) return;
    try {
      await api.post('/posts', { content: newPost, postType: 'MOTIVATION' });
      setNewPost('');
      fetchFeed();
    } catch (error) {
      console.error('Error creating post', error);
    }
  };

  return (
    <div className="animate-[fadeIn_0.4s_ease-out]">
      <header className="mb-8 flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-extrabold mb-1 bg-gradient-to-r from-accent-green to-accent-cyan bg-clip-text text-transparent">Social Feed</h1>
          <p className="text-slate-400 text-lg">See what your friends are up to</p>
        </div>
        <div className="flex bg-white/5 rounded-full p-1 border border-white/10">
          <button 
            className={`px-6 py-2 rounded-full font-medium transition-colors ${activeTab === 'feed' ? 'bg-accent-green text-dark' : 'text-slate-400 hover:text-slate-200'}`}
            onClick={() => setActiveTab('feed')}
          >
            Following
          </button>
          <button 
            className={`px-6 py-2 rounded-full font-medium transition-colors ${activeTab === 'discover' ? 'bg-accent-cyan text-dark' : 'text-slate-400 hover:text-slate-200'}`}
            onClick={() => setActiveTab('discover')}
          >
            Discover
          </button>
        </div>
      </header>
      
      <div className="glass-card mb-8">
        <textarea 
          placeholder="Share your health progress or a new goal..." 
          className="w-full bg-transparent border-none text-slate-100 text-lg resize-none outline-none mb-4 placeholder-slate-500" 
          rows={3}
          value={newPost}
          onChange={(e) => setNewPost(e.target.value)}
        />
        <div className="flex justify-end border-t border-white/10 pt-4">
          <button className="btn-primary" onClick={handlePost}>Post</button>
        </div>
      </div>
      
      <div className="flex flex-col">
        {loading ? (
          <div className="text-center text-slate-400 py-8">Loading feed...</div>
        ) : posts.length === 0 ? (
          <div className="text-center text-slate-400 glass-card py-12">No posts yet. Be the first to share!</div>
        ) : (
          posts.map(post => (
            <Post key={post.id} post={post} onRefresh={fetchFeed} />
          ))
        )}
      </div>
    </div>
  );
}
