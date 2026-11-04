export interface User {
    id?: string;
    name: string;
    email: string;
    password?: string;
    status: 'active' | 'inactive';
}
