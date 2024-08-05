echo "Make sure to execute this script from the root of the repository"

# Stop existing kube configs
kubectl delete ingress jqm-ingress
kubectl delete service jqm-lb
kubectl delete deployment jqm

# Package the application
cd ./jqm-all
/snap/intellij-idea-ultimate/510/plugins/maven/lib/maven3/bin/mvn clean install -DskipTests || exit 1
cd ..

# Build the image
docker build --no-cache -t enioka/jqm-standalone -f ./kubernetes-dbless/docker/Dockerfile . || exit 1

# Import the image into Microk8s, otherwise it will try to pull from dockerio
docker save -o ./jqm-standalone.tar enioka/jqm-standalone || exit 1
microk8s ctr image import jqm-standalone.tar || exit 1
rm jqm-standalone.tar

# Load the kubernetes configuration
kubectl apply -f ./kubernetes-dbless/jqm-deployment.yml
kubectl apply -f ./kubernetes-dbless/jqm-lb-service.yml
kubectl apply -f ./kubernetes-dbless/jqm-lb-ingress.yml  # If the `ingress` addon is enabled

# Wait for boot
sleep 30

# Verify the state of your kubernetes configuration
kubectl get all
