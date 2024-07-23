# JQM Kubernetes DB-less ("Standalone") deployment

This is the official Kubernetes DB-less setup for JQM (Job Queue Manager). It contains a deployment script and related
documentation.

In this document, "node" refers to a replicated instance, containing a JQM Engine, JQM Webservice, and HSQLDB
in-memory database.

## Limitations

JQM is originally designed to work with a central database aggregating jobs for active engines. Removing this central
database is not the intended use for it, and as such, some limitations are required to make it work in this way.

- The Kubernetes deployment is expected to use the `jqm-standalone` docker image, a variant of the Linux image with
  standalone-specific parameters enabled.

- Each individual node still requires a local database to function. For now, only in-memory HSQLDB is supported.

- Each individual node requires a unique IPv4 address. This IPv4 will be used by the node to assign job IDs. A node may
  only enqueue up to 1 million jobs, after that threshold, behavior is undefined and may lead to malfunctions, so the
  node should be restarted to clear its database. The `1789` port of each node needs to be exposed to other nodes.

- Each individual node hosts its own instance of the JQM REST API. When making requests to the JQM API, whether to
  enqueue a new job or control an existing job, the request should go through a simple load balancer. There is no
  additional logic to pick a "better suited" node for enqueue requests.

- Each individual node is completely unaware of other nodes. When queried for a job that does not belong to it, it
  guesses the corresponding IPv4 and forwards the request. No information about the state of other nodes is saved, it
  is up to the user to decide when a node is considered offline to avoid making unnecessary requests.

## Testing for developpers

Microk8s is recommended.

This guide will help you install it and properly alias `kubectl` for ease of use:
https://kubernetes.io/blog/2019/11/26/running-kubernetes-locally-on-linux-with-microk8s/

At the root of the repository:

```bash
# Package the application
mvn install -DskipTests

# Build the image
docker build -t enioka/jqm-standalone -f ./kubernetes-dbless/docker/Dockerfile .

# Import the image into Microk8s, otherwise it will try to pull from dockerio
docker save -o ./jqm-standalone.tar enioka/jqm-standalone
microk8s ctr image import jqm-standalone.tar
rm jqm-standalone.tar

# Load the deployment configuration
kubectl apply -f ./kubernetes-dbless/deployment.yml

# Load the loadbalancer configuration
kubectl apply -f ./kubernetes-dbless/service.yml

# Verify the state of your kubernetes configuration
kubectl get deployments
kubectl get pods

# If any pod is in a "READY: 0/1" state:
# Get an overview with
kubectl describe pod '<Pod id from the "kubectl get pods" command>'
# Get more detailed logs with
kubectl logs '<Pod id from the "kubectl get pods" command>' -c jqm-standalone-deployment
```

To remove the kubernetes setup:

```bash
kubectl delete service jqm-standalone-lb-service
kubectl delete deployment jqm-standalone-deployment
```

### Current issues

- SSL error when trying to connect to the cluster
