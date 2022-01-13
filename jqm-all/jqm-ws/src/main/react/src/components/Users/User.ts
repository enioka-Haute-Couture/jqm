export interface User {
    id?: number;
    login: string;
    email?: string;
    freeText?: string;
    internal?: boolean;
    locked: boolean;
    creationDate?: Date;
    expirationDate?: Date;
    roles: Array<number>;
}
