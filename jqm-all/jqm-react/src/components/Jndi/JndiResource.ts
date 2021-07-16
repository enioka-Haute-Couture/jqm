export interface JndiResource {
    id?: number;
    auth: string;
    name: string;
    type: string;
    description: string;
    singleton: boolean;
    factory: string;
    parameters: JndiParameter[];
}

export interface JndiParameter {
    id?: number;
    key: string;
    value: string | number | boolean;
}
