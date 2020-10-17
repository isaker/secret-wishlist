def auth_handler(event, context):
    print(event)
    auth_header = event['headers']['Authorization']
    print(auth_header)
    output_resource = get_output_resource(event)
    if not auth_header.startswith("Basic "):
        print('not authorized')
        return response("Deny", output_resource)


def response(allow, resource):
    return {
        "principalId": "user",
        "policyDocument": {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Action": "execute-api:Invoke",
                    "Effect": allow,
                    "Resource": resource
                }
            ]
        }
    }

def get_output_resource(event):
    method_arn = event['methodArn']
    http_method = event['httpMethod']
    output_resource = method_arn.split('/')[0] + '/*/' + http_method + '/'
    print(output_resource)
    return output_resource

