export interface User {
    id: string;
    name: string;
    email: string;
    role: string;
}

export interface UserInfo {
    name: string | null;
    email: string | null;
    role: string | null;
    issuer: string;
    subject: string;
}