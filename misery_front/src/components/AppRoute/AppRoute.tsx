import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from "../../pages/login";
import App from '../../App'
import Signup from '../../pages/Signup';
import WebAuthnRegister from '../../pages/WebAuthnRegister';
import WebAuthnLogin from '../../pages/WebAuthnLogin';

const PrivateRoute = ({ children }: { children: JSX.Element }) => {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" />;
};


function AppRouter() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/app" element={<PrivateRoute><App /></PrivateRoute>} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/" element={<Navigate to="/login" />}/>
        <Route path="/webauthn/login" element={<WebAuthnLogin />} />
        <Route path="/webauthn/register" element={<PrivateRoute><WebAuthnRegister /></PrivateRoute>} />
        
      </Routes>
    </Router>
  );
}

export default AppRouter;