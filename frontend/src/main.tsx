import { createRoot } from 'react-dom/client'
import './index.css'
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { WelcomePage } from './app/pages/WelcomePage';
import './i18n/i18n';
import { Toaster } from 'sonner';
import { ChatPage } from './app/pages/ChatPage';

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <Toaster position="top-center" richColors />
    <Routes>
      <Route path="/" element={<WelcomePage />} />
      <Route path="/chat" element={<ChatPage />} />
    </Routes>
  </BrowserRouter>,
)
