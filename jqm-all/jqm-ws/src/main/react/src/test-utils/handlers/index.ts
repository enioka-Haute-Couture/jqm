import { authHandlers } from "./auth";
import { classLoaderHandlers } from "./classLoaders";
import { clusterParameterHandlers } from "./clusterParameters";
import { jndiHandlers } from "./jndi";
import { jobDefinitionHandlers } from "./jobDefinitions";
import { mappingHandlers } from "./mappings";
import { nodeHandlers } from "./nodes";
import { queueHandlers } from "./queues";
import { roleHandlers } from "./roles";
import { runsHandlers } from "./runs";
import { userHandlers } from "./users";

export const handlers = [
    ...authHandlers,
    ...queueHandlers,
    ...roleHandlers,
    ...userHandlers,
    ...nodeHandlers,
    ...mappingHandlers,
    ...classLoaderHandlers,
    ...jndiHandlers,
    ...clusterParameterHandlers,
    ...jobDefinitionHandlers,
    ...runsHandlers,
];
