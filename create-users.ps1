param($n, $port, $prefix)
Write-Output "---"
Write-Output "$port -- $n"

# IFS=$'\n\t'

# NUM_CUSTOMERS=$1
# PORT=$2

For ($i=0; $i -lt $n; $i++) {
    $user_name=[guid]::NewGuid().ToString()
    # $customer_json="{'username': '${customer_name}', 'fullname': 'FULL NAME FOR ${customer_name}', 'email': '${customer_name}@example.com', 'phone': '613-555-1212', 'address': '${i} Fake St.' }"
    $user = @{
        userCode = "${prefix}_${i}";
        firstName = "${user_name}";
        lastName = "${user_name}";
        email = "${user_name}@example.com";
        address = "${i} Fake St.";
        password = "secret"
    }

    $endpoint = "http://localhost:$port/campaign-finance/api/v1_0/registration"
    Write-Output "---"
    Write-Output $user|ConvertTo-Json
    Invoke-WebRequest -Uri $endpoint -Method POST -Body ($user|ConvertTo-Json) -ContentType "application/json"
    # curl -H "Content-type: application/json" -X POST -d $CUSTOMER_JSON http://localhost:$PORT/customers
    # echo "---"
}