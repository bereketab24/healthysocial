# HealthySocial Frontend 💻

The HealthySocial frontend is a high-performance, responsive Single Page Application (SPA) built with React and TypeScript. It provides a sleek, modern interface for users to manage their wellness journey and connect with others.

## 🛠️ Technology Stack

- **Framework**: React 19
- **Build Tool**: Vite
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Authentication**: Keycloak-js (OIDC)
- **Routing**: React Router 7
- **HTTP Client**: Axios
- **Icons**: Lucide React

## 🚀 Features & Pages

- **Home Feed**: Real-time view of community posts and progress updates.
- **Habit Tracker**: Visual dashboard for managing habits, logging daily progress, and tracking streaks.
- **Goals Dashboard**: Progress tracking for long-term objectives with dynamic progress bars.
- **Community Challenges**: Browse and join wellness challenges.
- **User Profiles**: View personal stats and follow other users.

## 📐 Design Decisions

### 1. Modern Tooling with Vite
We chose **Vite** over Create React App (CRA) to ensure near-instant cold starts and lightning-fast Hot Module Replacement (HMR). This significantly improves developer productivity and results in a leaner production bundle.

### 2. Utility-First Styling
**Tailwind CSS** is used for all styling. This decision allows for:
- **Consistent Design**: Using a predefined scale for spacing, colors, and typography.
- **Responsive by Default**: Mobile-first design is baked into the workflow.
- **Performance**: Tailwind purges unused CSS, resulting in extremely small CSS files.

### 3. Secure Auth with Keycloak
Authentication is handled via the `@react-keycloak/web` philosophy (using custom `KeycloakContext`). 
- **Protected Routes**: Navigation is guarded based on authentication status.
- **Automatic Token Management**: Axios interceptors handle the injection of the Bearer token and automatic refreshing of expired tokens without user interruption.

### 4. Component-Based Architecture
The UI is broken down into small, reusable components (e.g., `Post`, `Sidebar`, `ProgressBar`). This ensures:
- **Maintainability**: Easier to test and update individual pieces of the UI.
- **Reusability**: Consistent UI elements across different pages.

## 🛠️ Development Setup

### Installation
```bash
cd frontend
npm install
```

### Running Locally
```bash
npm run dev
```
The app will be available at `http://localhost:5173`. 

*Note: The app is configured to proxy `/api` requests to `http://localhost:8080` to avoid CORS issues during development.*

### Building for Production
```bash
npm run build
```
The optimized production build will be generated in the `dist/` directory.

## 📂 Project Structure

```text
src/
├── api/            # Axios instance and API service calls
├── auth/           # Keycloak configuration and Auth Context
├── components/     # Reusable UI components
├── pages/          # Full page components (Home, Profile, etc.)
├── assets/         # Static assets (images, icons)
├── App.tsx         # Main routing and layout
└── main.tsx        # Application entry point
```

## 🎨 Styling System
The project uses a custom dark-themed palette defined in `tailwind.config.js`:
- `dark`: Deep background for reduced eye strain.
- `accent-green`: Success and habit completion indicators.
- `accent-cyan`: Informational and community elements.
- `slate`: Primary text and UI borders.
