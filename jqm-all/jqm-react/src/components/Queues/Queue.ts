export class Queue {
    id?: Number;
    name: string;
    defaultQueue: Boolean;
    description?: string;

    constructor(name: string, defaultQueue: Boolean, description?: string) {
        this.name = name;
        this.defaultQueue = defaultQueue;
        this.description = description;
    }
}
