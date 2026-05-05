import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api/axios';
import { useAuth } from '../auth/KeycloakContext';
import { Edit2, UserPlus, UserMinus, ShieldCheck } from 'lucide-react';
import { Post } from '../components/Post';

export function Profile() {
  const { id } = useParams();
  const { user } = useAuth();
  
  const [profile, setProfile] = useState<any>(null);
  const [posts, setPosts] = useState<any[]>([]);
  const [followers, setFollowers] = useState<any[]>([]);
  const [following, setFollowing] = useState<any[]>([]);
  const [isFollowing, setIsFollowing] = useState(false);
  
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({ bio: '', avatarUrl: '' });

  const isOwnProfile = id === 'me' || id === user?.sub;
  const targetId = isOwnProfile ? 'me' : id;

  const fetchProfileData = async () => {
    try {
      setLoading(true);
      // Fetch Profile
      const profileRes = await api.get(targetId === 'me' ? '/users/me' : `/users/${targetId}/profile`);
      setProfile(profileRes.data);
      if (isOwnProfile) {
        setEditForm({ bio: profileRes.data.bio || '', avatarUrl: profileRes.data.avatarUrl || '' });
      }
      
      const realUserId = profileRes.data.id;

      // Fetch Follow data
      const [followersRes, followingRes] = await Promise.all([
        api.get(`/users/${realUserId}/followers`),
        api.get(`/users/${realUserId}/following`)
      ]);
      setFollowers(followersRes.data || []);
      setFollowing(followingRes.data || []);

      // Check if current user follows this profile (if not own profile)
      if (!isOwnProfile && realUserId) {
        const isFollowRes = await api.get(`/users/${realUserId}/is-following`);
        setIsFollowing(isFollowRes.data?.isFollowing || false);
      }

      // Fetch User's Posts
      const postsRes = await api.get(`/posts/user/${realUserId}`);
      setPosts(postsRes.data.content || postsRes.data || []);

    } catch (error) {
      console.error('Error fetching profile', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfileData();
  }, [id]);

  const handleUpdateProfile = async () => {
    try {
      await api.patch('/users/me', editForm);
      setEditing(false);
      fetchProfileData();
    } catch (error) {
      console.error('Error updating profile', error);
    }
  };

  const toggleFollow = async () => {
    try {
      if (!profile?.id) return;
      if (isFollowing) {
        await api.delete(`/users/${profile.id}/follow`);
      } else {
        await api.post(`/users/${profile.id}/follow`);
      }
      setIsFollowing(!isFollowing);
      fetchProfileData(); // Refresh counts
    } catch (error) {
      console.error('Error toggling follow', error);
    }
  };

  if (loading) {
    return <div className="text-center text-slate-400 py-12">Loading profile...</div>;
  }

  if (!profile) {
    return <div className="text-center text-slate-400 py-12 glass-card">Profile not found.</div>;
  }

  return (
    <div className="animate-[fadeIn_0.4s_ease-out]">
      <header className="glass-card mb-8 relative overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-32 bg-gradient-to-r from-accent-green/20 to-accent-cyan/20"></div>
        
        <div className="relative pt-16 flex flex-col sm:flex-row items-center sm:items-end gap-6 pb-2">
          <div className="w-32 h-32 rounded-full bg-dark border-4 border-card flex items-center justify-center overflow-hidden shrink-0">
            {profile.avatarUrl ? (
              <img src={profile.avatarUrl} alt="Avatar" className="w-full h-full object-cover" />
            ) : (
              <span className="text-5xl text-accent-green font-bold uppercase">{(profile.username || 'U').charAt(0)}</span>
            )}
          </div>
          
          <div className="flex-1 text-center sm:text-left">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h1 className="text-3xl font-extrabold text-white mb-1 flex items-center justify-center sm:justify-start gap-2">
                  {profile.username || 'User'}
                  {profile.verified && <ShieldCheck className="text-accent-cyan" size={24} />}
                </h1>
                <p className="text-slate-400">@{profile.username?.toLowerCase() || 'user'}</p>
              </div>
              
              <div className="flex justify-center sm:justify-end gap-3">
                {isOwnProfile ? (
                  <button onClick={() => setEditing(!editing)} className="px-6 py-2 rounded-full bg-white/10 hover:bg-white/20 font-medium transition-colors flex items-center gap-2 cursor-pointer">
                    <Edit2 size={16} /> {editing ? 'Cancel Edit' : 'Edit Profile'}
                  </button>
                ) : (
                  <button 
                    onClick={toggleFollow} 
                    className={`px-6 py-2 rounded-full font-medium transition-colors flex items-center gap-2 cursor-pointer ${isFollowing ? 'bg-white/10 hover:bg-white/20' : 'bg-accent-cyan text-dark hover:bg-cyan-400'}`}
                  >
                    {isFollowing ? <><UserMinus size={16} /> Unfollow</> : <><UserPlus size={16} /> Follow</>}
                  </button>
                )}
              </div>
            </div>

            <div className="flex justify-center sm:justify-start gap-6 mt-6">
              <div className="flex flex-col items-center sm:items-start">
                <span className="text-2xl font-bold text-white">{followers.length || profile.followerCount || 0}</span>
                <span className="text-sm text-slate-400">Followers</span>
              </div>
              <div className="flex flex-col items-center sm:items-start">
                <span className="text-2xl font-bold text-white">{following.length || profile.followingCount || 0}</span>
                <span className="text-sm text-slate-400">Following</span>
              </div>
              <div className="flex flex-col items-center sm:items-start">
                <span className="text-2xl font-bold text-white">{posts.length}</span>
                <span className="text-sm text-slate-400">Posts</span>
              </div>
            </div>
          </div>
        </div>

        {editing ? (
          <div className="mt-8 pt-6 border-t border-white/10">
            <h3 className="font-bold text-white mb-4">Edit Profile</h3>
            <div className="flex flex-col gap-4">
              <input 
                type="text" 
                placeholder="Avatar URL" 
                className="w-full bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-accent-cyan"
                value={editForm.avatarUrl}
                onChange={e => setEditForm({...editForm, avatarUrl: e.target.value})}
              />
              <textarea 
                placeholder="Bio" 
                className="w-full bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 outline-none focus:border-accent-cyan"
                rows={3}
                value={editForm.bio}
                onChange={e => setEditForm({...editForm, bio: e.target.value})}
              />
              <div className="flex justify-end">
                <button onClick={handleUpdateProfile} className="btn-primary cursor-pointer">Save Changes</button>
              </div>
            </div>
          </div>
        ) : (
          profile.bio && (
            <div className="mt-8 pt-6 border-t border-white/10">
              <p className="text-slate-300">{profile.bio}</p>
            </div>
          )
        )}
      </header>

      <div className="mt-8">
        <h2 className="text-2xl font-bold text-white mb-6">Recent Posts</h2>
        <div className="flex flex-col gap-6">
          {posts.length === 0 ? (
            <div className="glass-card text-center text-slate-400 py-8">No posts yet.</div>
          ) : (
            posts.map(post => (
              <Post key={post.id} post={post} onRefresh={fetchProfileData} />
            ))
          )}
        </div>
      </div>
    </div>
  );
}
