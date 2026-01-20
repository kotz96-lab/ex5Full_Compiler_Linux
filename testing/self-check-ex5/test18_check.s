.data
msg_div_zero: .asciiz "Illegal Division By Zero"
msg_null_ptr: .asciiz "Invalid Pointer Dereference"
msg_bounds: .asciiz "Access Violation"
x_8: .word 0
p_10: .word 0

.text
.globl main

main:
    j __user_main    # Jump to user's main function
foo:

    # Temp_0 := 27
    li $t0, 27

    # RETURN Temp_0
    move $v0, $t0    # return value
    jr $ra

__user_main:

    # Allocate x_8
    # Allocate x_8

    # Temp_1 := foo()
    jal foo
    move $t0, $v0

    # x_8 := Temp_1
    sw $t0, x_8

    # Temp_2 := x_8
    lw $t0, x_8

    # p_10 := Temp_2
    sw $t0, p_10

    # Temp_3 := PrintInt()
    jal PrintInt
    move $t0, $v0

    # Exit program
    li $v0, 10
    syscall

error_div_by_zero:
    la $a0, msg_div_zero
    li $v0, 4
    syscall
    li $v0, 10
    syscall

error_null_pointer:
    la $a0, msg_null_ptr
    li $v0, 4
    syscall
    li $v0, 10
    syscall

error_bounds:
    la $a0, msg_bounds
    li $v0, 4
    syscall
    li $v0, 10
    syscall

PrintInt:
    # Print integer (expects value in p_10 variable)
    lw $a0, p_10    # load value to print
    li $v0, 1    # syscall: print_int
    syscall
    li $a0, 32    # print space
    li $v0, 11    # syscall: print_char
    syscall
    jr $ra    # return

# Program exit
    li $v0, 10
    syscall
