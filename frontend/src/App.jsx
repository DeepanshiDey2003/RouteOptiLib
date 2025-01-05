import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import NavBar from './NavBar';
import Home from './components/Home';
import ManageBlocks from './components/ManageBlocks';
import Properties from './components/Properties';
import './App.css';

function App() {
  return (
    <Router basename="/optilib-ui">
      <NavBar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/manage-blocks" element={<ManageBlocks />} />
        <Route path="/manage-properties" element={<Properties />} />
      </Routes>
    </Router>
  );
}

export default App;
