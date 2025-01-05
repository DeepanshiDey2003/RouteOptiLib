import { Link, useLocation } from 'react-router-dom';

function NavBar() {
  const location = useLocation();

  return (
    <div>
      <ul className='nav-links'>
        <li className={location.pathname === '/' ? 'active' : ''}>
          <Link to="/">Home</Link>
        </li>
        <li className={location.pathname === '/manage-blocks' ? 'active' : ''}>
          <Link to="/manage-blocks">Manage Blocks</Link>
        </li>
        <li className={location.pathname === '/manage-properties' ? 'active' : ''}>
          <Link to="/manage-properties">Manage Properties</Link>
        </li>
      </ul>
    </div>
  );
}

export default NavBar;
