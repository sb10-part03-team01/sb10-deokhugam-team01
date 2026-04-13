import {createHashRouter, RouteObject} from 'react-router-dom';
import Home from '@/app/(home)/page.tsx';
import BooksList from '@/app/books/page.tsx';
import BookDetail from '@/app/books/[id]/page.tsx';
import BookEditPage from '@/app/books/[id]/edit/page.tsx';
import BookRegister from '@/app/books/add/page.tsx';
import ReviewsList from '@/app/reviews/page.tsx';
import ReviewDetail from '@/app/reviews/[id]/page.tsx';
import SignUp from '@/app/auth/signup/page.tsx';
import Login from '@/app/auth/login/page.tsx';
import ClientLayout from "@/app/client-layout.tsx";
import NotFoundPage from "@/app/not-found.tsx"

const routes: RouteObject[] = [
  {
    path: '/',
    element: <ClientLayout/>,
    children: [
      {
        index: true,
        element: <Home/>,
      },
      {
        path: 'login',
        element: <Login/>,
      },
      {
        path: 'signup',
        element: <SignUp/>,
      },
      {
        path: 'books',
        children: [
          {
            index: true,
            element: <BooksList/>,
          },
          {
            path: ':id',
            children: [
              {
                index: true,
                element: <BookDetail/>,
              },
              {
                path: 'edit',
                element: <BookEditPage/>
              }
            ]
          },
          {
            path: 'add',
            element: <BookRegister/>,
          },
        ],
      },
      {
        path: 'reviews',
        children: [
          {
            index: true,
            element: <ReviewsList/>,
          },
          {
            path: ':id',
            element: <ReviewDetail/>,
          },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <NotFoundPage/>
  }
];

const router = createHashRouter(routes);

export default router; 