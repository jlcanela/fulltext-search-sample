order_purchase_timestamp
-> it’s in the brazilian timezone

assumption : purchase server is in sao paulo (GMT-3)
Time in São Paulo, State of São Paulo, Brazil

order_delivered_customer_date
-> it’s in the customer timezone

using the address of the customer 

olist_customers_dataset.csv
customer_state
code of two characters ?

if no timezone for dataset

create a reference table with (state, timezone)

strong suggestion: use UTC timezone everywhere 

timezone depends in location and period of the year



Initial request : 
- Identify all late deliveries, so that we can provide a 10% discount on next delivery to boost the sales


Option 1 : 
- "just compute the delay" 
- no timezone we do not care

Option 2 : 
- take into account the timezone 
- I do the computation using a reference table 

Add the information in the dataset 
- If user in brazil, select UTC-3 for adjusting the order_delivered_customer_date field 
- assumption is : it’s not a problem if we miss a few customers



Option 3 : 
- stop the garbage in 
- use UTC everywhere
- later it will be simpler for every new requests

Assignement 1. 


I’m a marketing people 
- I receive you result
  - I get the list of customers having late deliveries (more than 10 days)
- I can reproduce your result 
  - git clone <repository>
  - follow the README.md 

Evaluation
- I have a list of late customer IDs
- the list is "correct"
- I can reproduce your results
- I can understand how you did it 


Assignement 2: 

Provide an example of a bad character encoding

use a text in an encoding
consider the text in another encoding
----------

# Olist Assignement

Initial request : 
- Using olist dataset https://www.kaggle.com/olistbr/brazilian-ecommerce, identify all late deliveries, so that we can provide a 10% discount on next delivery to boost the sales
- order_purchase_timestamp is by default in Sao Paulo timezone
- order_delivered_customer_date is by default in the customer delivery address timezone 

You must provide the result: 
- A csv file with list of customers having late deliveries (more than 10 days)

You must explain how to reproduce the result: 
- git clone <repository>
- a README.md file explain how to reproduce the result 

Evaluation (100 points, 50 points required to have the module)
- git clone <repository> with a README.md: 10 points
- the README.md explain how to run the batch: 5 points
- following README.md, the batch is running without error: 5 points
- the batch generates a single CSV output file: 5 points
- the CSV output file contains a list of customer identifiers: 5 points
- all customer identifiers have a late delivery: 10 points
- all customers having a late delivery are exported: 10 points
- a procedure to run test is provided: 5 points
- a test coverage report is provided: 5 points
- all tests are meaningful and test coverage > 80% of line of code : 15 points
- the README.md explain how to package & run the batch on Amazon: 15 points
- an architecture document explaining the solution is provided, including diagram(s) and explanations: 10 points

