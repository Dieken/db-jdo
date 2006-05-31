/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.jdo.impl.jdoql.tree;

import org.apache.jdo.impl.jdoql.jdoqlc.JDOQLTokenTypes;
import org.apache.jdo.jdoql.tree.FloatLiteralExpression;
import org.apache.jdo.jdoql.tree.NodeVisitor;


/**
 * This node represents a float literal. It does not have any children.
 *
 * @author Michael Watzek
 */
public final class FloatLiteralExpr
    extends ConstantExpr implements FloatLiteralExpression
{
    /**
     * The noarg constructor is needed for ANTLR support and deserialization.
     * The caller must make sure to set the ANTLR tree structure himself
     * or, call <code>setChildren</code> optionally.
     */
    public FloatLiteralExpr()
    {}

    /**
     * The noarg constructor is needed for ANTLR support.
     * The caller must make sure to set the ANTLR tree structure himself
     * or, call <code>setChildren</code> optionally.
     */
    public FloatLiteralExpr(antlr.Token token)
    {   initialize( token );
    }

    /**
     * This constructor is called by the query tree instance.
     * It delegates to the super class constructor.
     * @param f the float value
     */
    FloatLiteralExpr(Float f)
    {   super( JDOQLTokenTypes.FLOAT_LITERAL, f.toString(), f ); //NOI18N
    }

    /**
     * This constructor is called by the query tree instance.
     * It delegates to the super class constructor.
     * @param f the float value
     */
    FloatLiteralExpr(float f)
    {   this( new Float(f) );
    }

    /**
     * Returns the float value represented by this expression.
     * @return the float value
     */
    public float getFloat()
    {   return ((Float)this.value).floatValue();
    }

    /**
     * Delegates to the argument <code>visitor</code>.
     * @param visitor the node visitor
     */
    public void arrive(NodeVisitor visitor)
    {   visitor.arrive( this );
    }

    /**
     * Delegates to the argument <code>visitor</code>.
     * @param visitor the node visitor
     * @param results the result array
     * @return the object returned by the visitor instance
     */
    public Object leave(NodeVisitor visitor, Object[] results)
    {   return visitor.leave( this, results );
    }
}