console.log("this is script file");

const toggleSidebar = () => {
    const sidebar = document.querySelector(".sidebar");
    const content = document.querySelector(".content");

    if (sidebar.style.display === "block") {
        // band karna hai
        sidebar.style.display = "none";
        content.style.marginLeft = "0%";
    } else {
        // show karna hai
        sidebar.style.display = "block";
        content.style.marginLeft = "20%";
    }
};


const search = () => {
    const searchInput = document.querySelector("#search-input");
    const searchResult = document.querySelector(".search-result");

    const query = searchInput.value;

    if (query === "") {
        searchResult.style.display = "none";
        return;
    }

    console.log(query);

    // sending request to server
    const url = `http://localhost:8282/search/${query}`;

    fetch(url)
        .then((response) => {
            return response.json();
        })
        .then((data) => {
            console.log(data);

            let text = `<div class="list-group">`;

            data.forEach((contact) => {
                text += `
                    <a href="/user/contact/${contact.cId}"
                       class="list-group-item list-group-item-action">
                        ${contact.name}
                    </a>
                `;
            });

            text += `</div>`;

            searchResult.innerHTML = text;
            searchResult.style.display = "block";
        });
};


// first request to server to create order
const paymentStart = () => {
    console.log("payment started...");

    const paymentField = document.querySelector("#payment_field");
    const amount = paymentField.value;

    if (amount === "" || amount === null) {
        alert("amount is required !!");
        return;
    }

    fetch("/user/create_order", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            amount: amount
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to create order");
        }
        return response.json();
    })
    .then(order => {
        console.log("Order created:", order);

        // Now open Razorpay checkout with this order
        var options = {
            "key": "rzp_test_TOpVdH40MBMjzI", // your API key (not secret)
            "amount": order.amount,
            "currency": order.currency,
            "name": "Acme Corp",
            "description": "Test Transaction",
			"image": "https://cdn.razorpay.com/logos/1234567890.png",
            "order_id": order.id,
			"handler": function (response) {
			    fetch("/user/verify-payment", {
			        method: "POST",
			        headers: { "Content-Type": "application/json" },
			        body: JSON.stringify({
			            razorpay_payment_id: response.razorpay_payment_id,
			            razorpay_order_id: response.razorpay_order_id,
			            razorpay_signature: response.razorpay_signature
			        })
			    })
			    .then(res => res.json())
			    .then(data => {
			        if (data.status === "success") {
			            alert("Payment successful and verified!");
			        } else {
			            alert("Payment verification failed!");
			        }
			    })
			    .catch(err => console.error("Verification error:", err));
			
            },
            "prefill": {
                "name": "",
                "email": "",
                "contact": ""
            },
			"notes":{
				"address":"Raxorpay Official"
			}
			,
            "theme": { "color": "#3399cc" }
        };

        var rzp = new Razorpay(options);

        rzp.on('payment.failed', function (response) {
            console.error("Payment failed:", response.error);
            alert("Payment failed: " + response.error.description);
        });

        rzp.open();
    })
    .catch(error => {
        console.error("Error creating order:", error);
        alert("Something went wrong while creating order");
    });
};