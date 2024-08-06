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

- Web Service Authentication is ***disabled***.

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

### Reach JQM from the host

To reach the load-balancer from outside the cluster, you will need either an Ingress or an external IP.

If you enable the `microk8s enable ingress` addon, you will be able to reach the port `1789` of your loadbalancer
through `localhost:80`. Only one port of your loadbalancer will be reachable. You also need to apply the
`jqm-lb-ingress.yml` config.

If you enable the `microk8s enable metallb` addon, you will be able to reach your loadbalancer with the IP assigned by
MetalLB (you can see it under the "EXTERNAL-IP" column when using `kubectl get all`).

Both methods can be active at the same time.

### Setup script

Run the following script at the root of the repository:

```bash
# Remove existing kube configs
kubectl delete ingress jqm-ingress
kubectl delete service jqm-lb
kubectl delete deployment jqm

# Build the image
docker build --no-cache -t enioka/jqm-standalone -f ./kubernetes-dbless/docker/Dockerfile . || exit 1

# Import the image into Microk8s, otherwise it will try to pull from dockerio
docker save -o ./jqm-standalone.tar enioka/jqm-standalone || exit 1
microk8s ctr image import jqm-standalone.tar || exit 1
rm jqm-standalone.tar

# Load the kubernetes configuration
kubectl apply -f ./kubernetes-dbless/jqm-deployment.yml
kubectl apply -f ./kubernetes-dbless/jqm-lb-service.yml
kubectl apply -f ./kubernetes-dbless/jqm-lb-ingress.yml  # Optional, only if the `ingress` addon is enabled

# Wait for boot
sleep 10

# Verify the state of your kubernetes configuration
kubectl get all
```

Other useful commands:
- `kubectl describe pod '<Pod id from the "kubectl get pods" command>'`: Get an overview of the pod and its state
- `kubectl logs -p '<Pod id from the "kubectl get pods" command>'`: Get the logs of the pod until that point
- `kubectl logs -f '<Pod id from the "kubectl get pods" command>'`: Stream the logs of the pod as they are arriving

When changing kubernetes yml's, only the corresponding `kubectl apply` commands need to be run.

To check that JQM is running, you can use `curl http://localhost:80/ws/simple/localnode/health` (or the load balancer's
EXTERNAL IP if ingress is disabled), it should reply with "Pollers are polling - IP: <pod ip>".
