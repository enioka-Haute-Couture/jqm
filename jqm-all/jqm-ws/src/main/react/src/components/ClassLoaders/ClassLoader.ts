export interface ClassLoader {
    id?: number;
    name: string;
    childFirst: boolean;
    hiddenClasses: string;
    tracingEnabled: boolean;
    persistent: boolean;
    allowedRunners: string;
}
