import base64
import boto3
import os

dynamodb = boto3.resource('dynamodb')

def auth_handler(event, context):
    print(event)
    auth_header = event['headers']['Authorization']
    print(auth_header)
    output_resource = event['methodArn']
    if not auth_header.startswith("Basic "):
        print('not authorized')
        return response("Deny", output_resource)

    id = event['pathParameters']['id']
    auth_value = auth_header.split(' ')[1]
    decoded_auth = base64.b64decode(auth_value).decode('ascii')
    username = decoded_auth.split(':')[0]

    if username != id:
        print('username and wishlist id not equal, denying access')
        return response("Deny", output_resource)

    secrets_table_name = os.environ['secretsTable']
    secrets_table = dynamodb.Table(secrets_table_name)
    db_response = secrets_table.get_item(
        Key={
            'id':id
        }
    )
    item = db_response.get('Item')
    print(item)
    if item is None:
        return response("Deny", output_resource)
    secret = item.get('secret', '')
    print(secret)
    print(auth_value)

    comp_string = base64.b64encode((id + ':' + secret).encode('ascii')).decode('ascii')
    print(comp_string)

    if comp_string == auth_value:
        print('comp_string == auth_value:')
        return response('Allow', output_resource)
    else:
        return response('Deny', output_resource)


def response(allow, resource):
    resp = {
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
    print(resp)
    return resp


def get_output_resource(event):
    method_arn = event['methodArn']
    http_method = event['httpMethod']
    output_resource = method_arn.split('/')[0] + '/*/' + http_method + '/'
    print(output_resource)
    return output_resource

