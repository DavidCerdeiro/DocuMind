import { createRoot } from 'react-dom/client'
import './index.css'
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { WelcomePage } from './app/pages/WelcomePage';
import './i18n/i18n';

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<WelcomePage />} />
    </Routes>
  </BrowserRouter>,
)
