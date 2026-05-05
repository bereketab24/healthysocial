import { useState, useEffect } from 'react';
import { api } from '../api/axios';
import { useAuth } from '../auth/KeycloakContext';
import { Heart, MessageCircle, Trash2, Send, Edit2, X } from 'lucide-react';

export function Post({ post, onRefresh }: { post: any, onRefresh: () => void }) {
  const { user } = useAuth();
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState<any[]>([]);
  const [newComment, setNewComment] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState(post.content);

  const fetchComments = async () => {
    try {
      const res = await api.get(`/posts/${post.id}/comments`);
      setComments(res.data.content || res.data || []);
    } catch (error) {
      console.error('Error fetching comments', error);
    }
  };

  useEffect(() => {
    if (showComments) fetchComments();
  }, [showComments]);

  const handleLike = async () => {
    try {
      await api.post(`/posts/${post.id}/like`);
      onRefresh();
    } catch (error) {
      console.error('Error liking post', error);
    }
  };

  const handleDelete = async () => {
    try {
      await api.delete(`/posts/${post.id}`);
      onRefresh();
    } catch (error) {
      console.error('Error deleting post', error);
    }
  };

  const handleEdit = async () => {
    try {
      await api.put(`/posts/${post.id}`, { content: editContent });
      setIsEditing(false);
      onRefresh();
    } catch (error) {
      console.error('Error editing post', error);
    }
  };

  const handleComment = async () => {
    if (!newComment.trim()) return;
    try {
      await api.post(`/posts/${post.id}/comments`, { content: newComment });
      setNewComment('');
      fetchComments();
      onRefresh(); // To update comment count
    } catch (error) {
      console.error('Error adding comment', error);
    }
  };

  const handleDeleteComment = async (commentId: string) => {
    try {
      await api.delete(`/posts/${post.id}/comments/${commentId}`);
      fetchComments();
      onRefresh();
    } catch (error) {
      console.error('Error deleting comment', error);
    }
  };

  const isAuthor = user?.sub === post.authorId || user?.sub === post.userId;

  return (
    <div className="glass-card mb-6">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-accent-green/20 border border-accent-green flex items-center justify-center font-bold text-accent-green uppercase">
            {(post.authorUsername || post.authorName || 'U').charAt(0)}
          </div>
          <div>
            <h4 className="font-semibold text-lg">{post.authorUsername || post.authorName || 'Unknown User'}</h4>
            <span className="text-sm text-slate-400">{new Date(post.createdAt).toLocaleString()}</span>
          </div>
        </div>
        {isAuthor && (
          <div className="flex gap-2">
            <button onClick={() => setIsEditing(!isEditing)} className="text-slate-500 hover:text-accent-cyan transition-colors cursor-pointer">
              {isEditing ? <X size={20} /> : <Edit2 size={20} />}
            </button>
            <button onClick={handleDelete} className="text-slate-500 hover:text-red-400 transition-colors cursor-pointer">
              <Trash2 size={20} />
            </button>
          </div>
        )}
      </div>

      {isEditing ? (
        <div className="mb-6">
          <textarea 
            className="w-full bg-black/20 border border-white/10 rounded-xl p-3 text-slate-100 mb-2 focus:border-accent-cyan outline-none"
            rows={3}
            value={editContent}
            onChange={e => setEditContent(e.target.value)}
          />
          <div className="flex justify-end gap-2">
            <button onClick={() => setIsEditing(false)} className="px-4 py-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors cursor-pointer">Cancel</button>
            <button onClick={handleEdit} className="px-4 py-2 rounded-lg bg-accent-cyan text-dark font-semibold hover:bg-cyan-400 transition-colors cursor-pointer">Save</button>
          </div>
        </div>
      ) : (
        <p className="text-slate-200 text-lg mb-6 whitespace-pre-wrap">{post.content}</p>
      )}
      
      <div className="flex items-center gap-6 border-t border-white/10 pt-4 text-slate-400">
        <button 
          onClick={handleLike} 
          className={`flex items-center gap-2 transition-colors cursor-pointer ${post.likedByCurrentUser ? 'text-accent-green' : 'hover:text-accent-green'}`}
        >
          <Heart size={20} className={post.likedByCurrentUser ? 'fill-current' : ''} />
          <span>{post.likesCount || post.likeCount || post.likes || 0}</span>
        </button>
        <button 
          onClick={() => setShowComments(!showComments)}
          className={`flex items-center gap-2 transition-colors cursor-pointer ${showComments ? 'text-accent-cyan' : 'hover:text-accent-cyan'}`}
        >
          <MessageCircle size={20} className={showComments ? 'fill-current' : ''} />
          <span>{post.commentsCount || post.commentCount || post.comments?.length || 0} Comments</span>
        </button>
      </div>

      {showComments && (
        <div className="mt-4 pt-4 border-t border-white/5">
          <div className="flex gap-3 mb-6">
             <input 
               type="text" 
               placeholder="Write a comment..." 
               className="flex-1 bg-black/20 border border-white/10 rounded-full px-4 py-2 text-sm text-slate-100 outline-none focus:border-accent-cyan"
               value={newComment}
               onChange={e => setNewComment(e.target.value)}
               onKeyDown={e => e.key === 'Enter' && handleComment()}
             />
             <button onClick={handleComment} className="w-10 h-10 rounded-full bg-accent-cyan flex items-center justify-center text-dark hover:bg-cyan-400 transition-colors cursor-pointer">
               <Send size={16} />
             </button>
          </div>
          <div className="flex flex-col gap-4">
            {comments.map(c => (
              <div key={c.id} className="flex gap-3 text-sm">
                 <div className="w-8 h-8 shrink-0 rounded-full bg-accent-cyan/20 border border-accent-cyan flex items-center justify-center font-bold text-accent-cyan uppercase text-xs">
                    {(c.authorUsername || c.authorName || 'U').charAt(0)}
                 </div>
                 <div className="flex-1 bg-white/5 rounded-2xl rounded-tl-sm p-3 relative group">
                    <span className="font-semibold block mb-1 text-accent-cyan">{c.authorUsername || c.authorName}</span>
                    <p className="text-slate-300">{c.content}</p>
                    {(user?.sub === c.authorId || user?.sub === c.userId) && (
                      <button 
                        onClick={() => handleDeleteComment(c.id)}
                        className="absolute top-3 right-3 text-slate-500 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
                      >
                        <X size={14} />
                      </button>
                    )}
                 </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
