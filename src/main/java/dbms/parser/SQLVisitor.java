// Generated from /Users/mathewhany/Code/dbms/SQL.g4 by ANTLR 4.12.0
package dbms.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SQLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SQLParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(SQLParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(SQLParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#insert}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert(SQLParser.InsertContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate(SQLParser.UpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#assignments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignments(SQLParser.AssignmentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(SQLParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete(SQLParser.DeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn(SQLParser.ColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#conditions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditions(SQLParser.ConditionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#compound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompound(SQLParser.CompoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(SQLParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator(SQLParser.OperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SQLParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(SQLParser.ValueContext ctx);
}