import sys

a = 0
b = 0

a = int(input("Enter first number: "))
b = int(input("Enter second number: "))
operation = input("Enter operation: ")

if operation == "+":
    print(a + b)
elif operation == "-":
    print(a - b)
elif operation == "*":
    print(a * b)
elif operation == "/":
    print(a / b)
else:
    print("Invalid operation")

sys.exit(0)