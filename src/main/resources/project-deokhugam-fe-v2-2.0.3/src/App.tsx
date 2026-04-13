import {RouterProvider} from 'react-router-dom';
import './globals.css';
import "./styles/datepickerStyle.css";
import router from './routes.tsx';


function App() {
  return (
      <RouterProvider router={router}/>
  );
}

export default App;
