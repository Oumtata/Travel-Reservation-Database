./run_rmi.sh > /dev/null

echo "Edit file run_middleware.sh to include instructions for launching the middleware"
echo '  $1 - hostname of Flights hostname is lab2-xx'
echo '  $2 - hostname of Cars'
echo '  $3 - hostname of Rooms'
echo '  $4 - hostname of Customer'

java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware $1 $2 $3 $4
