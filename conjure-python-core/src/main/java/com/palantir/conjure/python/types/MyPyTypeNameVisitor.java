/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.python.types;

import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.visitor.TypeDefinitionVisitor;
import com.palantir.conjure.visitor.TypeVisitor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The mypy type for the conjure type.
 */
public final class MyPyTypeNameVisitor implements Type.Visitor<String> {

    private final Set<TypeName> typesByName;

    public MyPyTypeNameVisitor(List<TypeDefinition> types) {
        this.typesByName = types.stream().map(type ->
                type.accept(TypeDefinitionVisitor.TYPE_NAME)).collect(Collectors.toSet());
    }

    @Override
    public String visitList(ListType type) {
        return "List[" + type.getItemType().accept(this) + "]";
    }

    @Override
    public String visitMap(MapType type) {
        return "Dict[" + type.getKeyType().accept(this) + ", " + type.getValueType().accept(this) + "]";
    }

    @Override
    public String visitOptional(OptionalType type) {
        return "Optional[" + type.getItemType().accept(this) + "]";
    }

    @Override
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public String visitPrimitive(PrimitiveType type) {
        switch (type.get()) {
            case STRING:
            case RID:
            case BEARERTOKEN:
            case UUID:
            case DATETIME:
                return "str";
            case BOOLEAN:
                return "bool";
            case DOUBLE:
                return "float";
            case INTEGER:
            case SAFELONG:
                return "int";
            case ANY:
            case BINARY:
                return "Any";
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    @Override
    public String visitReference(TypeName type) {
        // Types without namespace are either defined locally in this conjure definition, or raw imports.
        if (typesByName.contains(type)) {
            return type.getName();
        } else {
            throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    @Override
    public String visitExternal(ExternalReference externalReference) {
        if (externalReference.getFallback().accept(TypeVisitor.IS_PRIMITIVE)) {
            return visitPrimitive(externalReference.getFallback().accept(TypeVisitor.PRIMITIVE));
        } else {
            throw new IllegalArgumentException("unknown type: " + externalReference);
        }
    }

    @Override
    public String visitUnknown(String unknownType) {
        throw new IllegalArgumentException("unknown type: " + unknownType);
    }

    @Override
    public String visitSet(SetType type) {
        // TODO(#27): real sets
        return Type.list(ListType.of(type.getItemType())).accept(this);
    }

}
