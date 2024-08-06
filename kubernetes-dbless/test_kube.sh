# This test script assumes $REPLICAS (default 4) of JQM-Standlone are currently running and reachable through a
# load-balancer listening to $URL (default http://localhost:80). It will then allow itself $ATTEMPTS (default 20)
# attempts for each test to succeed.

# How many replicas to test are
REPLICAS="${REPLICAS:-4}"
# How many attempts for each test (either how many before failure or how many times to test)
ATTEMPTS="${ATTEMPTS:-20}"
# The base URL of the load balancer
URL="${URL:-http://localhost:80}"

########################################################################################################################
########################################################################################################################

echo ""
echo "##########################################"
echo "# TEST 1/3 - Find the IP of all replicas #"
echo ""

# History of the IP's that were found
declare -a ips

# Actual test
attempt=0
while [ ${#ips[@]} -lt $REPLICAS ] && [ $attempt -lt $ATTEMPTS ]
do
    sleep 1
    attempt=$((attempt + 1))
    echo "Attempt $attempt/$ATTEMPTS:"

    result=`curl -s $URL/ws/simple/localnode/health`
    if ! [[ $result =~ ^Pollers\ are\ polling\ -\ IP:\  ]]
    then
        echo "  /!\\ curl resulted in a server error /!\\"
        echo "$result"
        continue
    fi

    ip=${result#"Pollers are polling - IP: "}
    if [[ ${ips[@]} =~ "$ip" ]]
    then
        echo "  IP $ip already found before"
    else
        ips+=("$ip")
        echo "  IP $ip added to cache, ${#ips[@]}/$REPLICAS found"
    fi
done

echo "Found replicas:"
printf ' %s\n' "${ips[@]}"

echo ""
echo "# TEST 1/3 - RESULT                      #"
if [ ${#ips[@]} -ne $REPLICAS ]
then
    echo "# KO: Could not find all replicas        #"
    echo "##########################################"
    exit 1
fi
echo "# OK: Found all replicas                 #"
echo "##########################################"
echo ""

########################################################################################################################
########################################################################################################################

echo ""
echo "##########################################"
echo "# TEST 2/3 - Check IDs of new jobs       #"
echo ""

# Bash equivalent of `StandaloneHeplers.ipFromId()` method
function ipFromId {
    base=$(($1 / 1000000))
    ip=""
    ip+=$(($base / 1000 / 1000 / 1000 % 1000))
    ip+="."
    ip+=$(($base / 1000 / 1000 % 1000))
    ip+="."
    ip+=$(($base / 1000 % 1000))
    ip+="."
    ip+=$(($base % 1000))
    echo $ip
}
if [ `ipFromId 192168024001000156` != "192.168.24.1" ]
then
    echo "function ipFromId is not working"
    exit 1
fi

# History of the IP's that were able to be tested
declare -a tested_ips

# Actual test
attempt=0
failures=0
last_success=0
while [ ${#tested_ips[@]} -lt $REPLICAS ] && [ $attempt -lt $ATTEMPTS ]
do
    sleep 1
    attempt=$((attempt + 1))
    echo "Attempt $attempt/$ATTEMPTS:"

    #result=`curl -s -H "Content-Type: application/json" -d "{}" $URL/ws/client/ji`  # Enqueue on '/client', payload ?
    result=`curl -s -H "Content-Type: application/x-www-form-urlencoded" -d "applicationname=DemoEcho" $URL/ws/simple/ji`
    if ! [[ $result =~ ^[0-9]+$ ]]
    then
        echo "  /!\\ curl resulted in a server error /!\\"
        echo "$result"
        continue
    fi

    id=$result
    ip=`ipFromId $id`
    if [[ ${ips[@]} =~ "$ip" ]]
    then
        echo "  OK: ID $id matches IP $ip"
        last_success=$id

        if [[ ${tested_ips[@]} =~ "$ip" ]]
        then
            echo "  IP $ip already tested before"
        else
            tested_ips+=("$ip")
            echo "  IP $ip added to test cache, ${#tested_ips[@]}/$REPLICAS tested"
        fi
    else
        echo "  KO: ID $id does not match any replica's IP"
        failures=$((failures + 1))
    fi
done

echo ""
echo "# TEST 2/3 - RESULT                      #"
if [ $failures -gt 0 ]
then
    printf '# KO: %02d/%02d attempts failed              #\n' $failures $attempt
    echo "##########################################"
    exit 1
fi
if [ ${#tested_ips[@]} -ne $REPLICAS ]
then
    echo "# KO: Could not test all replicas        #"
    echo "##########################################"
    exit 1
fi
echo "# OK: All IDs were correct               #"
echo "##########################################"
echo ""

########################################################################################################################
########################################################################################################################

echo ""
echo "##########################################"
echo "# TEST 3/3 - Check all replicas find job #"
echo ""

# Actual test
attempt=0
failures=0
while [ $attempt -lt $ATTEMPTS ]  # No way to verify which replicas were hit with this test
do
    sleep 1
    attempt=$((attempt + 1))
    echo "Attempt $attempt/$ATTEMPTS:"

    #result=`curl -s $URL/ws/client/ji/192168024001000156`  # Force a redirect to a non-existent node to test error case
    result=`curl -s $URL/ws/client/ji/$last_success`  # Normal call
    #     JSON version of the output          XML version of the output
    if [[ "$result" =~ ^\{\"id\":.*$ ]] || [[ "$result" =~ ^\<\?xml\ version=\"1.0\"\ encoding=\"UTF-8\"\ standalone=\"yes\"\?\>\<jobInstance\>\<id\>.*$ ]]
    then
        echo "  OK: Job found"
    else
        echo "  KO: Job not found. Error:"

        echo ""
        stacktrace=`expr "$result" : '^.*\("userReadableMessage".*$\)'`
        [ -z "$stacktrace" ] && echo "$result" || echo "$stacktrace"
        echo ""

        failures=$((failures + 1))
    fi
done

echo ""
echo "# TEST 3/3 - RESULT                      #"
if [ $failures -gt 0 ]
then
    printf '# KO: %02d/%02d attempts failed              #\n' $failures $attempt
    echo "##########################################"
    exit 1
fi
echo "# OK: All attempts were successful       #"
echo "##########################################"
echo ""
